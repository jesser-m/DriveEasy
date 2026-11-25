package com.driveeasy.location.remote;

import com.driveeasy.location.exception.BadRequestException;
import com.driveeasy.location.exception.ConflictException;
import com.driveeasy.location.exception.ExternalServiceException;
import com.driveeasy.location.exception.ResourceNotFoundException;
import java.util.function.Supplier;
import org.springframework.web.client.HttpClientErrorException;

/** Traduit les erreurs HTTP / reseau des appels inter-services en exceptions metier. */
final class RemoteCalls {

    private RemoteCalls() {
    }

    static <T> T call(String service, String ressource, Supplier<T> action) {
        try {
            return action.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(ressource + " introuvable");
        } catch (HttpClientErrorException.Conflict e) {
            throw new ConflictException(service + " a refuse l'operation (409) : " + ressource);
        } catch (HttpClientErrorException e) {
            throw new BadRequestException(service + " a rejete la requete (" + e.getStatusCode().value() + ")");
        } catch (RuntimeException e) {
            // service arrete, timeout, aucune instance dans Eureka, erreur 5xx...
            throw new ExternalServiceException(service + " est indisponible", e);
        }
    }

    static void run(String service, String ressource, Runnable action) {
        call(service, ressource, () -> {
            action.run();
            return Boolean.TRUE;
        });
    }
}
