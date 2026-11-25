package com.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/** dateRetourEffectif optionnelle (defaut : aujourd'hui). */
public record RetourRequest(@NotNull @PositiveOrZero Integer kmRetour, LocalDate dateRetourEffectif) {
}
