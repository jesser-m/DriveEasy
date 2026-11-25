package com.driveeasy.location.remote;

import java.math.BigDecimal;

public record VehiculeDto(Long id, String immatriculation, String marque, String modele, Integer annee, String couleur,
                          String categorie, String carburant, String boite, Integer nbPlaces, Integer kilometrage,
                          BigDecimal prixJournalier, String statut, String imageUrl) {
}
