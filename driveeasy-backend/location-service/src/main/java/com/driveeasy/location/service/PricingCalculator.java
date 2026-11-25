package com.driveeasy.location.service;

import com.driveeasy.location.config.LocationProperties;
import com.driveeasy.location.dto.PriceDetail;
import com.driveeasy.location.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/**
 * Prix = jours x prix journalier x coefficient categorie - remise fidelite (FIDELE 5 %, VIP 10 %).
 * Penalite de retard = 1,5 x prix journalier x jours de retard.
 */
@Component
public class PricingCalculator {

    private final LocationProperties props;

    public PricingCalculator(LocationProperties props) {
        this.props = props;
    }

    public PriceDetail calculer(BigDecimal prixJournalier, String categorieVehicule, String categorieClient,
                                LocalDate debut, LocalDate fin) {
        long jours = ChronoUnit.DAYS.between(debut, fin);
        if (jours < 1) {
            throw new BadRequestException("La location doit durer au moins 1 jour");
        }
        BigDecimal coefficient = props.coefficientPour(categorieVehicule);
        BigDecimal sousTotal = prixJournalier.multiply(coefficient).multiply(BigDecimal.valueOf(jours))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remisePourcent = props.remisePour(categorieClient);
        BigDecimal remise = sousTotal.multiply(remisePourcent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = sousTotal.subtract(remise).setScale(2, RoundingMode.HALF_UP);
        return new PriceDetail(jours, prixJournalier, categorieVehicule, coefficient, sousTotal, categorieClient,
                remisePourcent, remise, total);
    }

    public BigDecimal penalite(BigDecimal prixJournalierApplique, LocalDate finPrevue, LocalDate retourEffectif) {
        long retard = ChronoUnit.DAYS.between(finPrevue, retourEffectif);
        if (retard <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return prixJournalierApplique.multiply(props.penaliteMultiplicateur())
                .multiply(BigDecimal.valueOf(retard)).setScale(2, RoundingMode.HALF_UP);
    }
}
