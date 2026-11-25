package com.driveeasy.location.dto;

import jakarta.validation.constraints.PositiveOrZero;

/** Corps optionnel : si kmDepart est absent, le kilometrage du vehicule est utilise. */
public record DepartRequest(@PositiveOrZero Integer kmDepart) {
}
