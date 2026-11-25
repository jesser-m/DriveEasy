package com.driveeasy.client.remote;

import com.driveeasy.client.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Appel vers location-service (via Eureka + load balancing) : le client a-t-il une location active ? */
@Component
public class LocationServiceClient {

    private final RestClient rest;

    public LocationServiceClient(RestClient.Builder builder,
                                 @Value("${driveeasy.services.location-url:http://location-service}") String baseUrl) {
        this.rest = builder.clone().baseUrl(baseUrl).build();
    }

    public boolean aDesLocationsActives(Long clientId) {
        try {
            ActiveCount r = rest.get().uri("/locations/clients/{id}/actives/count", clientId)
                    .retrieve().body(ActiveCount.class);
            return r != null && r.actives() > 0;
        } catch (RuntimeException e) {
            throw new ExternalServiceException("location-service est indisponible : suppression impossible a verifier", e);
        }
    }

    record ActiveCount(Long clientId, long actives) {
    }
}
