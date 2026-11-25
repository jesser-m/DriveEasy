package com.driveeasy.location.dto;

import com.driveeasy.location.model.StatutLocation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record StatistiquesResponse(LocalDate debut, LocalDate fin, BigDecimal chiffreAffaires, double tauxOccupation,
                                   int nombreVehicules, Map<StatutLocation, Long> locationsParStatut,
                                   List<TopVehicule> topVehicules) {

    public record TopVehicule(Long vehiculeId, String libelle, long nombreLocations) {
    }
}
