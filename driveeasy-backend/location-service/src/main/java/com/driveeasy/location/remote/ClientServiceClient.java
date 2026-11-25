package com.driveeasy.location.remote;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Appels synchrones vers client-service (http://client-service resolu par Eureka). */
@Component
public class ClientServiceClient {

    private static final String SERVICE = "client-service";
    private final RestClient rest;

    public ClientServiceClient(RestClient.Builder builder,
                               @Value("${driveeasy.services.client-url:http://client-service}") String baseUrl) {
        this.rest = builder.clone().baseUrl(baseUrl).build();
    }

    public ClientDto get(Long id) {
        return RemoteCalls.call(SERVICE, "Client " + id,
                () -> rest.get().uri("/clients/{id}", id).retrieve().body(ClientDto.class));
    }

    /** GET /clients/{id}/eligibilite */
    public EligibiliteDto eligibilite(Long id) {
        return RemoteCalls.call(SERVICE, "Client " + id,
                () -> rest.get().uri("/clients/{id}/eligibilite", id).retrieve().body(EligibiliteDto.class));
    }

    /** PATCH /clients/{id}/categorie */
    public void mettreAJourCategorie(Long id, String categorie) {
        RemoteCalls.run(SERVICE, "Client " + id,
                () -> rest.patch().uri("/clients/{id}/categorie", id)
                        .body(Map.of("categorieClient", categorie)).retrieve().toBodilessEntity());
    }
}
