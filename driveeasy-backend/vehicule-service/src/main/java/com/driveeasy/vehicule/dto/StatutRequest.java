package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.StatutVehicule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Corps du PATCH /vehicules/{id}/statut (kilometrage optionnel, renseigne au retour). */
public record StatutRequest(@NotNull StatutVehicule statut, @PositiveOrZero Integer kilometrage) {
}
