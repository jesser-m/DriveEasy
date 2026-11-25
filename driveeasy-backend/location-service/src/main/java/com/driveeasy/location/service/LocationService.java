package com.driveeasy.location.service;

import com.driveeasy.location.config.LocationProperties;
import com.driveeasy.location.dto.DepartRequest;
import com.driveeasy.location.dto.LocationDetailResponse;
import com.driveeasy.location.dto.LocationRequest;
import com.driveeasy.location.dto.LocationResponse;
import com.driveeasy.location.dto.PageResponse;
import com.driveeasy.location.dto.PriceDetail;
import com.driveeasy.location.dto.RetourRequest;
import com.driveeasy.location.dto.StatistiquesResponse;
import com.driveeasy.location.exception.BadRequestException;
import com.driveeasy.location.exception.ConflictException;
import com.driveeasy.location.exception.ResourceNotFoundException;
import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.remote.ClientDto;
import com.driveeasy.location.remote.ClientServiceClient;
import com.driveeasy.location.remote.EligibiliteDto;
import com.driveeasy.location.remote.VehiculeDto;
import com.driveeasy.location.remote.VehiculeServiceClient;
import com.driveeasy.location.repository.LocationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orchestration du cycle de vie d'une location (appels vers client-service et vehicule-service). */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repository;
    private final ClientServiceClient clients;
    private final VehiculeServiceClient vehicules;
    private final PricingCalculator pricing;
    private final LocationProperties props;

    public LocationService(LocationRepository repository, ClientServiceClient clients, VehiculeServiceClient vehicules,
                           PricingCalculator pricing, LocationProperties props) {
        this.repository = repository;
        this.clients = clients;
        this.vehicules = vehicules;
        this.pricing = pricing;
        this.props = props;
    }

    // ------------------------------------------------------------------ simulation / creation

    public PriceDetail simuler(LocationRequest req) {
        validerPeriode(req.dateDebut(), req.dateFinPrevue());
        ClientDto client = clients.get(req.clientId());
        VehiculeDto vehicule = vehicules.get(req.vehiculeId());
        return pricing.calculer(vehicule.prixJournalier(), vehicule.categorie(), client.categorieClient(),
                req.dateDebut(), req.dateFinPrevue());
    }

    /**
     * Creation d'une reservation. Les verifications (eligibilite, disponibilite) sont faites AVANT toute ecriture :
     * si un appel inter-services echoue, rien n'est cree (pas d'etat incoherent).
     */
    public synchronized LocationResponse creer(LocationRequest req) {
        validerPeriode(req.dateDebut(), req.dateFinPrevue());

        ClientDto client = clients.get(req.clientId());
        EligibiliteDto eligibilite = clients.eligibilite(req.clientId());
        if (!eligibilite.eligible()) {
            throw new ConflictException("Client non eligible : " + String.join(" ; ", eligibilite.motifs()));
        }
        if (client.dateExpirationPermis().isBefore(req.dateFinPrevue())) {
            throw new ConflictException("Le permis expire le " + client.dateExpirationPermis()
                    + ", avant la fin de la location");
        }

        VehiculeDto vehicule = vehicules.get(req.vehiculeId());
        if (!"DISPONIBLE".equals(vehicule.statut())) {
            throw new ConflictException("Vehicule non disponible (statut : " + vehicule.statut() + ")");
        }
        if (repository.existeChevauchement(req.vehiculeId(), req.dateDebut(), req.dateFinPrevue())) {
            throw new ConflictException("Le vehicule est deja reserve sur cette periode");
        }

        PriceDetail prix = pricing.calculer(vehicule.prixJournalier(), vehicule.categorie(), client.categorieClient(),
                req.dateDebut(), req.dateFinPrevue());
        Location location = Location.builder()
                .clientId(req.clientId()).vehiculeId(req.vehiculeId())
                .dateDebut(req.dateDebut()).dateFinPrevue(req.dateFinPrevue())
                .prixJournalierApplique(vehicule.prixJournalier())
                .montantTotal(prix.montantTotal()).penalite(BigDecimal.ZERO.setScale(2))
                .statut(StatutLocation.RESERVEE).dateCreation(LocalDateTime.now()).build();
        return LocationResponse.from(repository.save(location));
    }

    // ------------------------------------------------------------------ cycle de vie

    /** Depart : vehicule -> LOUE (vehicule-service), location -> EN_COURS. Compensation si echec. */
    public synchronized LocationResponse demarrer(Long id, DepartRequest req) {
        Location l = trouver(id);
        if (l.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Seule une location RESERVEE peut demarrer (statut : " + l.getStatut() + ")");
        }
        VehiculeDto vehicule = vehicules.get(l.getVehiculeId());
        if (!"DISPONIBLE".equals(vehicule.statut())) {
            throw new ConflictException("Le vehicule n'est pas disponible (statut : " + vehicule.statut() + ")");
        }
        vehicules.changerStatut(l.getVehiculeId(), "LOUE", null);
        try {
            int kmDepart = (req != null && req.kmDepart() != null) ? req.kmDepart() : vehicule.kilometrage();
            Location maj = l.toBuilder().statut(StatutLocation.EN_COURS).kmDepart(kmDepart).build();
            return LocationResponse.from(repository.save(maj));
        } catch (RuntimeException e) {
            log.error("Echec du depart, compensation : vehicule remis a DISPONIBLE", e);
            vehicules.changerStatut(l.getVehiculeId(), "DISPONIBLE", null);
            throw e;
        }
    }

    /** Retour : calcul des penalites, vehicule -> DISPONIBLE, location -> TERMINEE, mise a jour de la fidelite. */
    public synchronized LocationResponse retourner(Long id, RetourRequest req) {
        Location l = trouver(id);
        if (l.getStatut() != StatutLocation.EN_COURS) {
            throw new ConflictException("Seule une location EN_COURS peut etre retournee (statut : " + l.getStatut() + ")");
        }
        LocalDate retour = req.dateRetourEffectif() != null ? req.dateRetourEffectif() : LocalDate.now();
        if (retour.isBefore(l.getDateDebut())) {
            throw new BadRequestException("La date de retour precede la date de debut");
        }
        if (req.kmRetour() < l.getKmDepart()) {
            throw new BadRequestException("kmRetour (" + req.kmRetour() + ") < kmDepart (" + l.getKmDepart() + ")");
        }
        BigDecimal penalite = pricing.penalite(l.getPrixJournalierApplique(), l.getDateFinPrevue(), retour);

        // 1) liberer le vehicule (si ca echoue : exception, la location reste EN_COURS)
        vehicules.changerStatut(l.getVehiculeId(), "DISPONIBLE", req.kmRetour());
        Location terminee;
        try {
            // 2) cloturer la location
            Location maj = l.toBuilder().kmRetour(req.kmRetour()).dateRetourEffectif(retour).penalite(penalite)
                    .montantTotal(l.getMontantTotal().add(penalite)).statut(StatutLocation.TERMINEE).build();
            terminee = repository.save(maj);
        } catch (RuntimeException e) {
            log.error("Echec de la cloture, compensation : vehicule remis a LOUE", e);
            vehicules.changerStatut(l.getVehiculeId(), "LOUE", null);
            throw e;
        }
        // 3) fidelite (non bloquant : le retour est deja valide)
        mettreAJourFidelite(terminee.getClientId());
        return LocationResponse.from(terminee);
    }

    public synchronized LocationResponse annuler(Long id) {
        Location l = trouver(id);
        if (l.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Seule une location RESERVEE peut etre annulee (statut : " + l.getStatut() + ")");
        }
        return LocationResponse.from(repository.save(l.toBuilder().statut(StatutLocation.ANNULEE).build()));
    }

    private void mettreAJourFidelite(Long clientId) {
        long terminees = repository.compterTerminees(clientId);
        String cible = terminees >= props.seuilVip() ? "VIP" : terminees >= props.seuilFidele() ? "FIDELE" : null;
        if (cible == null) {
            return;
        }
        try {
            ClientDto client = clients.get(clientId);
            if (rang(cible) > rang(client.categorieClient())) {
                clients.mettreAJourCategorie(clientId, cible);
                log.info("Client {} passe en categorie {} ({} locations terminees)", clientId, cible, terminees);
            }
        } catch (RuntimeException e) {
            log.warn("Mise a jour de la categorie du client {} impossible : {}", clientId, e.getMessage());
        }
    }

    private int rang(String categorie) {
        return "VIP".equals(categorie) ? 2 : "FIDELE".equals(categorie) ? 1 : 0;
    }

    // ------------------------------------------------------------------ consultation

    public LocationDetailResponse detail(Long id) {
        Location l = trouver(id);
        return new LocationDetailResponse(LocationResponse.from(l),
                tolerant(() -> clients.get(l.getClientId())), tolerant(() -> vehicules.get(l.getVehiculeId())));
    }

    public PageResponse<LocationResponse> rechercher(StatutLocation statut, Long clientId, Long vehiculeId,
                                                     LocalDate debut, LocalDate fin, int page, int size) {
        int taille = Math.max(1, Math.min(size, 100));
        int numero = Math.max(0, page);
        List<Location> filtrees = repository.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> debut == null || !l.getDateFinPrevue().isBefore(debut))
                .filter(l -> fin == null || !l.getDateDebut().isAfter(fin))
                .sorted(Comparator.comparing(Location::getId).reversed()).toList();
        int from = Math.min(numero * taille, filtrees.size());
        int to = Math.min(from + taille, filtrees.size());
        List<LocationResponse> contenu = filtrees.subList(from, to).stream().map(LocationResponse::from).toList();
        return new PageResponse<>(contenu, numero, taille, filtrees.size(), (int) Math.ceil(filtrees.size() / (double) taille));
    }

    public long compterActives(Long clientId) {
        return repository.compterActives(clientId);
    }

    /** Vehicules disponibles pour une periode : DISPONIBLES (vehicule-service) et sans reservation qui chevauche. */
    public List<VehiculeDto> vehiculesDisponibles(LocalDate debut, LocalDate fin) {
        if (!fin.isAfter(debut)) {
            throw new BadRequestException("dateFin doit etre posterieure a dateDebut");
        }
        return vehicules.disponibles(debut, fin, repository.vehiculesOccupes(debut, fin));
    }

    // ------------------------------------------------------------------ statistiques

    public StatistiquesResponse statistiques(LocalDate from, LocalDate to) {
        LocalDate debut = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate fin = to != null ? to : LocalDate.now();
        if (fin.isBefore(debut)) {
            throw new BadRequestException("'to' doit etre posterieure a 'from'");
        }
        List<Location> toutes = repository.findAll();

        BigDecimal ca = toutes.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE && l.getDateRetourEffectif() != null
                        && !l.getDateRetourEffectif().isBefore(debut) && !l.getDateRetourEffectif().isAfter(fin))
                .map(Location::getMontantTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        long joursPeriode = ChronoUnit.DAYS.between(debut, fin) + 1;
        long joursLoues = toutes.stream()
                .filter(l -> l.getStatut() == StatutLocation.EN_COURS || l.getStatut() == StatutLocation.TERMINEE)
                .mapToLong(l -> joursDansPeriode(l, debut, fin)).sum();
        int nbVehicules = tolerantInt(() -> vehicules.compter());
        double taux = nbVehicules == 0 ? 0.0
                : Math.min(100.0, Math.round(joursLoues * 10000.0 / (nbVehicules * joursPeriode)) / 100.0);

        Map<StatutLocation, Long> parStatut = toutes.stream().collect(
                Collectors.groupingBy(Location::getStatut, () -> new EnumMap<>(StatutLocation.class), Collectors.counting()));

        List<StatistiquesResponse.TopVehicule> top = toutes.stream()
                .filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                .collect(Collectors.groupingBy(Location::getVehiculeId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new StatistiquesResponse.TopVehicule(e.getKey(), libelle(e.getKey()), e.getValue()))
                .toList();
        return new StatistiquesResponse(debut, fin, ca, taux, nbVehicules, parStatut, top);
    }

    private long joursDansPeriode(Location l, LocalDate from, LocalDate to) {
        LocalDate finLoc = l.getDateRetourEffectif() != null ? l.getDateRetourEffectif() : l.getDateFinPrevue();
        LocalDate s = l.getDateDebut().isAfter(from) ? l.getDateDebut() : from;
        LocalDate fin = to.plusDays(1);
        LocalDate e = finLoc.isBefore(fin) ? finLoc : fin;
        return Math.max(0, ChronoUnit.DAYS.between(s, e));
    }

    private String libelle(Long vehiculeId) {
        VehiculeDto v = tolerant(() -> vehicules.get(vehiculeId));
        return v != null ? v.marque() + " " + v.modele() + " (" + v.immatriculation() + ")" : "Vehicule #" + vehiculeId;
    }

    // ------------------------------------------------------------------ utilitaires

    private Location trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location " + id + " introuvable"));
    }

    private void validerPeriode(LocalDate debut, LocalDate fin) {
        if (!fin.isAfter(debut)) {
            throw new BadRequestException("dateFinPrevue doit etre posterieure a dateDebut");
        }
        if (debut.isBefore(LocalDate.now())) {
            throw new BadRequestException("dateDebut ne peut pas etre dans le passe");
        }
    }

    private <T> T tolerant(Supplier<T> action) {
        try {
            return action.get();
        } catch (RuntimeException e) {
            log.warn("Donnee distante indisponible : {}", e.getMessage());
            return null;
        }
    }

    private int tolerantInt(Supplier<Integer> action) {
        Integer v = tolerant(action);
        return v == null ? 0 : v;
    }
}
