package com.driveeasy.location.dto;

/** Nombre de locations actives (RESERVEE / EN_COURS) d'un client - utilise par client-service. */
public record ActiveCountResponse(Long clientId, long actives) {
}
