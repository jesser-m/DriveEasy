package com.driveeasy.location.dto;

import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LocationResponse(Long id, Long clientId, Long vehiculeId, LocalDate dateDebut, LocalDate dateFinPrevue,
                               LocalDate dateRetourEffectif, Integer kmDepart, Integer kmRetour,
                               BigDecimal prixJournalierApplique, BigDecimal montantTotal, BigDecimal penalite,
                               StatutLocation statut, LocalDateTime dateCreation) {

    public static LocationResponse from(Location l) {
        return new LocationResponse(l.getId(), l.getClientId(), l.getVehiculeId(), l.getDateDebut(),
                l.getDateFinPrevue(), l.getDateRetourEffectif(), l.getKmDepart(), l.getKmRetour(),
                l.getPrixJournalierApplique(), l.getMontantTotal(), l.getPenalite(), l.getStatut(), l.getDateCreation());
    }
}
