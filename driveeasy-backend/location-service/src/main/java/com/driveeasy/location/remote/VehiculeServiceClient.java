package com.driveeasy.location.remote;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Appels synchrones vers vehicule-service (http://vehicule-service resolu par Eureka). */
@Component
public class VehiculeServiceClient {

    private static final String SERVICE = "vehicule-service";
    private final RestClient rest;

    public VehiculeServiceClient(RestClient.Builder builder,
                                 @Value("${driveeasy.services.vehicule-url:http://vehicule-service}") String baseUrl) {
        this.rest = builder.clone().baseUrl(baseUrl).build();
    }

    public VehiculeDto get(Long id) {
        return RemoteCalls.call(SERVICE, "Vehicule " + id,
                () -> rest.get().uri("/vehicules/{id}", id).retrieve().body(VehiculeDto.class));
    }

    public int compter() {
        List<VehiculeDto> tous = RemoteCalls.call(SERVICE, "Liste des vehicules",
                () -> rest.get().uri("/vehicules").retrieve().body(new ParameterizedTypeReference<List<VehiculeDto>>() {
                }));
        return tous == null ? 0 : tous.size();
    }

    /** PATCH /vehicules/{id}/statut (kilometrage facultatif). */
    public void changerStatut(Long id, String statut, Integer kilometrage) {
        RemoteCalls.run(SERVICE, "Vehicule " + id,
                () -> rest.patch().uri("/vehicules/{id}/statut", id)
                        .body(new StatutPayload(statut, kilometrage)).retrieve().toBodilessEntity());
    }

    /** GET /vehicules/disponibles?dateDebut&dateFin&exclureIds */
    public List<VehiculeDto> disponibles(LocalDate debut, LocalDate fin, Collection<Long> exclureIds) {
        String exclus = exclureIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<VehiculeDto> r = RemoteCalls.call(SERVICE, "Vehicules disponibles",
                () -> rest.get().uri("/vehicules/disponibles?dateDebut={d}&dateFin={f}&exclureIds={e}", debut, fin, exclus)
                        .retrieve().body(new ParameterizedTypeReference<List<VehiculeDto>>() {
                        }));
        return r == null ? List.of() : r;
    }

    record StatutPayload(String statut, Integer kilometrage) {
    }
}
