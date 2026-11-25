package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.Boite;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.Categorie;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.model.Vehicule;
import java.math.BigDecimal;

public record VehiculeResponse(Long id, String immatriculation, String marque, String modele, Integer annee,
                               String couleur, Categorie categorie, Carburant carburant, Boite boite,
                               Integer nbPlaces, Integer kilometrage, BigDecimal prixJournalier,
                               StatutVehicule statut, String imageUrl) {

    public static VehiculeResponse from(Vehicule v) {
        return new VehiculeResponse(v.getId(), v.getImmatriculation(), v.getMarque(), v.getModele(), v.getAnnee(),
                v.getCouleur(), v.getCategorie(), v.getCarburant(), v.getBoite(), v.getNbPlaces(),
                v.getKilometrage(), v.getPrixJournalier(), v.getStatut(), v.getImageUrl());
    }
}
