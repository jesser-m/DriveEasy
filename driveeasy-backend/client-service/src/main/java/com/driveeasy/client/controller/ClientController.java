package com.driveeasy.client.controller;

import com.driveeasy.client.dto.CategorieRequest;
import com.driveeasy.client.dto.ClientRequest;
import com.driveeasy.client.dto.ClientResponse;
import com.driveeasy.client.dto.EligibiliteResponse;
import com.driveeasy.client.exception.ConflictException;
import com.driveeasy.client.exception.ResourceNotFoundException;
import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;
import com.driveeasy.client.remote.LocationServiceClient;
import com.driveeasy.client.repository.ClientRepository;
import com.driveeasy.client.service.EligibiliteChecker;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
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
@RequestMapping("/clients")
public class ClientController {

    private final ClientRepository repository;
    private final EligibiliteChecker eligibiliteChecker;
    private final LocationServiceClient locationClient;

    public ClientController(ClientRepository repository, EligibiliteChecker eligibiliteChecker,
                            LocationServiceClient locationClient) {
        this.repository = repository;
        this.eligibiliteChecker = eligibiliteChecker;
        this.locationClient = locationClient;
    }

    /** Recherche : q (nom, prenom, CIN ou email) ou filtres nom / cin / email. */
    @GetMapping
    public List<ClientResponse> rechercher(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) String nom,
                                           @RequestParam(required = false) String cin,
                                           @RequestParam(required = false) String email) {
        return repository.findAll().stream()
                .filter(c -> q == null || q.isBlank() || contient(c.getNom(), q) || contient(c.getPrenom(), q)
                        || contient(c.getCin(), q) || contient(c.getEmail(), q))
                .filter(c -> nom == null || contient(c.getNom(), nom) || contient(c.getPrenom(), nom))
                .filter(c -> cin == null || contient(c.getCin(), cin))
                .filter(c -> email == null || contient(c.getEmail(), email))
                .map(ClientResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClientResponse detail(@PathVariable Long id) {
        return ClientResponse.from(trouver(id));
    }

    @PostMapping
    public synchronized ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest req) {
        verifierUnicite(req, null);
        Client c = Client.builder().build();
        appliquer(c, req);
        c.setCategorieClient(req.categorieClient() != null ? req.categorieClient() : CategorieClient.STANDARD);
        c.setDateInscription(LocalDate.now());
        repository.save(c);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(c.getId()).toUri();
        return ResponseEntity.created(location).body(ClientResponse.from(c));
    }

    @PutMapping("/{id}")
    public synchronized ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest req) {
        Client c = trouver(id);
        verifierUnicite(req, id);
        appliquer(c, req);
        if (req.categorieClient() != null) {
            c.setCategorieClient(req.categorieClient());
        }
        return ClientResponse.from(repository.save(c));
    }

    /** Refusee (409) si le client a une location active (RESERVEE ou EN_COURS) : verifie aupres de location-service. */
    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<Void> supprimer(@PathVariable Long id) {
        trouver(id);
        if (locationClient.aDesLocationsActives(id)) {
            throw new ConflictException("Suppression refusee : le client a une location active");
        }
        repository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/eligibilite")
    public EligibiliteResponse eligibilite(@PathVariable Long id) {
        return eligibiliteChecker.verifier(trouver(id), LocalDate.now());
    }

    /** Utilise par location-service (FIDELE apres 5 locations terminees, VIP apres 15). */
    @PatchMapping("/{id}/categorie")
    public synchronized ClientResponse changerCategorie(@PathVariable Long id, @Valid @RequestBody CategorieRequest req) {
        Client c = trouver(id);
        c.setCategorieClient(req.categorieClient());
        return ClientResponse.from(repository.save(c));
    }

    // ------------------------------------------------------------------ utilitaires

    private Client trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client " + id + " introuvable"));
    }

    private boolean contient(String source, String recherche) {
        return source != null && source.toLowerCase().contains(recherche.trim().toLowerCase());
    }

    private void verifierUnicite(ClientRequest req, Long excludeId) {
        if (repository.existsByCin(req.cin(), excludeId)) {
            throw new ConflictException("CIN deja utilise : " + req.cin());
        }
        if (repository.existsByEmail(req.email(), excludeId)) {
            throw new ConflictException("Email deja utilise : " + req.email());
        }
        if (repository.existsByNumeroPermis(req.numeroPermis(), excludeId)) {
            throw new ConflictException("Numero de permis deja utilise : " + req.numeroPermis());
        }
    }

    private void appliquer(Client c, ClientRequest r) {
        c.setCin(r.cin());
        c.setNom(r.nom());
        c.setPrenom(r.prenom());
        c.setDateNaissance(r.dateNaissance());
        c.setEmail(r.email().trim().toLowerCase());
        c.setTelephone(r.telephone());
        c.setAdresse(r.adresse());
        c.setNumeroPermis(r.numeroPermis().trim());
        c.setDateExpirationPermis(r.dateExpirationPermis());
    }
}
