package com.driveeasy.vehicule.exception;

/** Appel inter-services impossible (service indisponible, timeout...). */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
