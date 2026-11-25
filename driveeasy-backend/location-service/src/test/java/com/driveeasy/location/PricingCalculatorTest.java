package com.driveeasy.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.driveeasy.location.config.LocationProperties;
import com.driveeasy.location.dto.PriceDetail;
import com.driveeasy.location.exception.BadRequestException;
import com.driveeasy.location.service.PricingCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PricingCalculatorTest {

    private final LocationProperties props = new LocationProperties(new BigDecimal("1.5"), 5, 15,
            new BigDecimal("5"), new BigDecimal("10"), Map.of("BERLINE", new BigDecimal("1.2")));
    private final PricingCalculator calculator = new PricingCalculator(props);

    @Test
    void prixStandardSansRemise() {
        PriceDetail p = calculator.calculer(new BigDecimal("100"), "BERLINE", "STANDARD",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        assertEquals(3, p.jours());
        assertEquals(0, new BigDecimal("360.00").compareTo(p.montantTotal()));
    }

    @Test
    void remiseVipDixPourCent() {
        PriceDetail p = calculator.calculer(new BigDecimal("100"), "BERLINE", "VIP",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        assertEquals(0, new BigDecimal("324.00").compareTo(p.montantTotal()));
    }

    @Test
    void remiseFideleCinqPourCent() {
        PriceDetail p = calculator.calculer(new BigDecimal("100"), "BERLINE", "FIDELE",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));
        assertEquals(0, new BigDecimal("342.00").compareTo(p.montantTotal()));
    }

    @Test
    void penaliteDeRetard() {
        BigDecimal pen = calculator.penalite(new BigDecimal("100"), LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 6));
        assertEquals(0, new BigDecimal("300.00").compareTo(pen));
    }

    @Test
    void pasDePenaliteSiRetourALHeure() {
        BigDecimal pen = calculator.penalite(new BigDecimal("100"), LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 4));
        assertEquals(0, BigDecimal.ZERO.compareTo(pen));
    }

    @Test
    void dureeNulleRefusee() {
        assertThrows(BadRequestException.class, () -> calculator.calculer(new BigDecimal("100"), "BERLINE", "STANDARD",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 1)));
    }
}
