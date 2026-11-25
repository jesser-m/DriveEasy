package com.driveeasy.client.service;

import com.driveeasy.client.dto.EligibiliteResponse;
import com.driveeasy.client.model.Client;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Regle metier : age >= 21 ans et permis non expire. */
@Component
public class EligibiliteChecker {

    private final int ageMinimum;

    public EligibiliteChecker(@Value("${driveeasy.client.age-minimum:21}") int ageMinimum) {
        this.ageMinimum = ageMinimum;
    }

    public EligibiliteResponse verifier(Client c, LocalDate aujourdhui) {
        int age = Period.between(c.getDateNaissance(), aujourdhui).getYears();
        boolean permisValide = !c.getDateExpirationPermis().isBefore(aujourdhui);
        List<String> motifs = new ArrayList<>();
        if (age < ageMinimum) {
            motifs.add("Age minimum requis : " + ageMinimum + " ans (age actuel : " + age + ")");
        }
        if (!permisValide) {
            motifs.add("Permis de conduire expire le " + c.getDateExpirationPermis());
        }
        return new EligibiliteResponse(c.getId(), motifs.isEmpty(), age, permisValide, motifs);
    }
}
