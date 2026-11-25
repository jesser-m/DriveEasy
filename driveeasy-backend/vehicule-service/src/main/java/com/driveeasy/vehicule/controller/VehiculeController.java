package com.driveeasy.vehicule.controller;

import com.driveeasy.vehicule.dto.StatutRequest;
import com.driveeasy.vehicule.dto.VehiculeRequest;
import com.driveeasy.vehicule.dto.VehiculeResponse;
import com.driveeasy.vehicule.exception.BadRequestException;
import com.driveeasy.vehicule.exception.ConflictException;
import com.driveeasy.vehicule.exception.ResourceNotFoundException;
import com.driveeasy.vehicule.model.Boite;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.Categorie;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.model.Vehicule;
import com.driveeasy.vehicule.repository.VehiculeRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeRepository repository;

    @Value("${driveeasy.vehicule.annee-minimale:1990}")
    private int anneeMinimale;

    public VehiculeController(VehiculeRepository repository) {
        this.repository = repository;
    }

    /** Liste avec filtres optionnels : categorie, carburant, statut, prixMax. */
    @GetMapping
    public List<VehiculeResponse> lister(@RequestParam(required = false) Categorie categorie,
                                         @RequestParam(required = false) Carburant carburant,
                                         @RequestParam(required = false) StatutVehicule statut,
                                         @RequestParam(required = false) BigDecimal prixMax) {
        return repository.findAll().stream()
                .filter(v -> categorie == null || v.getCategorie() == categorie)
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .map(VehiculeResponse::from).toList();
    }

    /** Vehicules DISPONIBLES pour une periode ; exclureIds = vehicules deja reserves (fourni par location-service). */
    @GetMapping("/disponibles")
    public List<VehiculeResponse> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false, defaultValue = "") List<Long> exclureIds) {
        if (dateFin.isBefore(dateDebut)) {
            throw new BadRequestException("dateFin doit etre posterieure a dateDebut");
        }
        return repository.findAll().stream()
                .filter(v -> v.getStatut() == StatutVehicule.DISPONIBLE)
                .filter(v -> !exclureIds.contains(v.getId()))
                .map(VehiculeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public VehiculeResponse detail(@PathVariable Long id) {
        return VehiculeResponse.from(trouver(id));
    }

    @PostMapping
    public synchronized ResponseEntity<VehiculeResponse> creer(@Valid @RequestBody VehiculeRequest req) {
        verifier(req, null);
        if (req.statut() == StatutVehicule.LOUE) {
            throw new BadRequestException("Un nouveau vehicule ne peut pas etre cree avec le statut LOUE");
        }
        Vehicule v = Vehicule.builder().build();
        appliquer(v, req);
        v.setStatut(req.statut() != null ? req.statut() : StatutVehicule.DISPONIBLE);
        repository.save(v);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(v.getId()).toUri();
        return ResponseEntity.created(location).body(VehiculeResponse.from(v));
    }

    @PutMapping("/{id}")
    public synchronized VehiculeResponse modifier(@PathVariable Long id, @Valid @RequestBody VehiculeRequest req) {
        Vehicule v = trouver(id);
        verifier(req, id);
        appliquer(v, req);
        if (req.statut() != null) {
            v.setStatut(req.statut());
        }
        return VehiculeResponse.from(repository.save(v));
    }

    /** Refusee (409) si le vehicule est loue. */
    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<Void> supprimer(@PathVariable Long id) {
        Vehicule v = trouver(id);
        if (v.getStatut() == StatutVehicule.LOUE) {
            throw new ConflictException("Impossible de supprimer un vehicule actuellement loue");
        }
        repository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /** Utilise par location-service (depart -> LOUE, retour -> DISPONIBLE + kilometrage). */
    @PatchMapping("/{id}/statut")
    public synchronized VehiculeResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest req) {
        Vehicule v = trouver(id);
        if (req.statut() == StatutVehicule.LOUE && v.getStatut() != StatutVehicule.DISPONIBLE) {
            throw new ConflictException("Le vehicule est " + v.getStatut() + " : il ne peut pas etre loue");
        }
        if (req.kilometrage() != null) {
            if (req.kilometrage() < v.getKilometrage()) {
                throw new BadRequestException("Le kilometrage ne peut pas diminuer");
            }
            v.setKilometrage(req.kilometrage());
        }
        v.setStatut(req.statut());
        return VehiculeResponse.from(repository.save(v));
    }

    // ------------------------------------------------------------------ utilitaires

    private Vehicule trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule " + id + " introuvable"));
    }

    /** Regles de gestion : annee coherente, immatriculation unique. */
    private void verifier(VehiculeRequest req, Long excludeId) {
        int courante = Year.now().getValue();
        if (req.annee() > courante || req.annee() < anneeMinimale) {
            throw new BadRequestException("L'annee doit etre comprise entre " + anneeMinimale + " et " + courante);
        }
        if (repository.existsByImmatriculation(req.immatriculation().trim(), excludeId)) {
            throw new ConflictException("Immatriculation deja utilisee : " + req.immatriculation());
        }
    }

    private void appliquer(Vehicule v, VehiculeRequest r) {
        v.setImmatriculation(r.immatriculation().trim().toUpperCase());
        v.setMarque(r.marque());
        v.setModele(r.modele());
        v.setAnnee(r.annee());
        v.setCouleur(r.couleur());
        v.setCategorie(r.categorie());
        v.setCarburant(r.carburant());
        v.setBoite(r.boite());
        v.setNbPlaces(r.nbPlaces());
        v.setKilometrage(r.kilometrage());
        v.setPrixJournalier(r.prixJournalier());
        v.setImageUrl(r.imageUrl());
    }
}
