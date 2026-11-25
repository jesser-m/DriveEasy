package com.driveeasy.location.config;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Proprietes metier externalisees dans le Config Server (location-service.yml). */
@ConfigurationProperties(prefix = "driveeasy.location")
public record LocationProperties(BigDecimal penaliteMultiplicateur, int seuilFidele, int seuilVip,
                                 BigDecimal remiseFidele, BigDecimal remiseVip,
                                 Map<String, BigDecimal> coefficients) {

    public LocationProperties {
        TreeMap<String, BigDecimal> copie = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (coefficients != null) {
            copie.putAll(coefficients);
        }
        coefficients = copie;
        if (penaliteMultiplicateur == null) {
            penaliteMultiplicateur = new BigDecimal("1.5");
        }
        if (remiseFidele == null) {
            remiseFidele = new BigDecimal("5");
        }
        if (remiseVip == null) {
            remiseVip = new BigDecimal("10");
        }
    }

    public BigDecimal coefficientPour(String categorieVehicule) {
        BigDecimal c = categorieVehicule == null ? null : coefficients.get(categorieVehicule);
        return c != null ? c : BigDecimal.ONE;
    }

    /** Remise de fidelite en pourcentage (FIDELE : 5 %, VIP : 10 %). */
    public BigDecimal remisePour(String categorieClient) {
        if ("FIDELE".equalsIgnoreCase(categorieClient)) {
            return remiseFidele;
        }
        if ("VIP".equalsIgnoreCase(categorieClient)) {
            return remiseVip;
        }
        return BigDecimal.ZERO;
    }
}
