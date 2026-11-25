package com.driveeasy.location.dto;

import java.math.BigDecimal;

/** Detail du calcul de prix (utilise aussi comme reponse de la simulation). */
public record PriceDetail(long jours, BigDecimal prixJournalier, String categorieVehicule,
                          BigDecimal coefficientCategorie, BigDecimal sousTotal, String categorieClient,
                          BigDecimal remisePourcent, BigDecimal montantRemise, BigDecimal montantTotal) {
}
