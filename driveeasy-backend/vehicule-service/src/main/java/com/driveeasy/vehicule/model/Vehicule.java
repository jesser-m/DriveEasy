package com.driveeasy.vehicule.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entite metier (stockee en RAM). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {
    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private Integer annee;
    private String couleur;
    private Categorie categorie;
    private Carburant carburant;
    private Boite boite;
    private Integer nbPlaces;
    private Integer kilometrage;
    private BigDecimal prixJournalier;
    private StatutVehicule statut;
    private String imageUrl;
}
