package com.driveeasy.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.driveeasy.client.dto.EligibiliteResponse;
import com.driveeasy.client.model.Client;
import com.driveeasy.client.service.EligibiliteChecker;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EligibiliteCheckerTest {

    private final EligibiliteChecker checker = new EligibiliteChecker(21);
    private final LocalDate today = LocalDate.of(2026, 11, 14);

    private Client client(LocalDate naissance, LocalDate expiration) {
        return Client.builder().id(1L).dateNaissance(naissance).dateExpirationPermis(expiration).build();
    }

    @Test
    void clientMajeurAvecPermisValideEstEligible() {
        EligibiliteResponse r = checker.verifier(client(LocalDate.of(2000, 1, 1), LocalDate.of(2030, 1, 1)), today);
        assertTrue(r.eligible());
        assertEquals(26, r.age());
    }

    @Test
    void clientDeMoinsDe21AnsNestPasEligible() {
        EligibiliteResponse r = checker.verifier(client(LocalDate.of(2008, 1, 1), LocalDate.of(2030, 1, 1)), today);
        assertFalse(r.eligible());
        assertEquals(1, r.motifs().size());
    }

    @Test
    void permisExpireNestPasEligible() {
        EligibiliteResponse r = checker.verifier(client(LocalDate.of(1990, 1, 1), LocalDate.of(2026, 1, 1)), today);
        assertFalse(r.eligible());
        assertFalse(r.permisValide());
    }
}
