package com.driveeasy.location.controller;

import com.driveeasy.location.dto.ActiveCountResponse;
import com.driveeasy.location.dto.DepartRequest;
import com.driveeasy.location.dto.LocationDetailResponse;
import com.driveeasy.location.dto.LocationRequest;
import com.driveeasy.location.dto.LocationResponse;
import com.driveeasy.location.dto.PageResponse;
import com.driveeasy.location.dto.PriceDetail;
import com.driveeasy.location.dto.RetourRequest;
import com.driveeasy.location.dto.StatistiquesResponse;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.remote.VehiculeDto;
import com.driveeasy.location.service.LocationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private static final DateTimeFormat.ISO ISO_DATE = DateTimeFormat.ISO.DATE;

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    /** Liste paginee, filtres : statut, clientId, vehiculeId, periode (dateDebut / dateFin). */
    @GetMapping
    public PageResponse<LocationResponse> lister(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.rechercher(statut, clientId, vehiculeId, dateDebut, dateFin, page, size);
    }

    @GetMapping("/{id}")
    public LocationDetailResponse detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PostMapping
    public ResponseEntity<LocationResponse> creer(@Valid @RequestBody LocationRequest req) {
        LocationResponse cree = service.creer(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(uri).body(cree);
    }

    @PostMapping("/simulation")
    public PriceDetail simuler(@Valid @RequestBody LocationRequest req) {
        return service.simuler(req);
    }

    @PatchMapping("/{id}/depart")
    public LocationResponse depart(@PathVariable Long id, @Valid @RequestBody(required = false) DepartRequest req) {
        return service.demarrer(id, req);
    }

    @PatchMapping("/{id}/retour")
    public LocationResponse retour(@PathVariable Long id, @Valid @RequestBody RetourRequest req) {
        return service.retourner(id, req);
    }

    @PatchMapping("/{id}/annulation")
    public LocationResponse annulation(@PathVariable Long id) {
        return service.annuler(id);
    }

    /** CA, taux d'occupation, top vehicules (par defaut : mois en cours). */
    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.statistiques(from, to);
    }

    /** Vehicules reellement disponibles sur une periode (etape 3 de l'assistant "Nouvelle location"). */
    @GetMapping("/disponibilites")
    public List<VehiculeDto> disponibilites(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.vehiculesDisponibles(dateDebut, dateFin);
    }

    /** Utilise par client-service avant suppression d'un client. */
    @GetMapping("/clients/{clientId}/actives/count")
    public ActiveCountResponse activesDuClient(@PathVariable Long clientId) {
        return new ActiveCountResponse(clientId, service.compterActives(clientId));
    }
}
