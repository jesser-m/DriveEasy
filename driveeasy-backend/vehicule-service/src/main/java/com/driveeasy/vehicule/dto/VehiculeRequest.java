package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.Boite;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.Categorie;
import com.driveeasy.vehicule.model.StatutVehicule;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record VehiculeRequest(
        @NotBlank String immatriculation,
        @NotBlank String marque,
        @NotBlank String modele,
        @NotNull @Min(1900) Integer annee,
        String couleur,
        @NotNull Categorie categorie,
        @NotNull Carburant carburant,
        @NotNull Boite boite,
        @NotNull @Min(1) @Max(9) Integer nbPlaces,
        @NotNull @PositiveOrZero Integer kilometrage,
        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "doit etre strictement positif") BigDecimal prixJournalier,
        StatutVehicule statut,
        String imageUrl) {
}
