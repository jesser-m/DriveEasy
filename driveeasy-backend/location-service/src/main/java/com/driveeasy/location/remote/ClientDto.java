package com.driveeasy.location.remote;

import java.time.LocalDate;

public record ClientDto(Long id, String cin, String nom, String prenom, LocalDate dateNaissance, String email,
                        String telephone, String adresse, String numeroPermis, LocalDate dateExpirationPermis,
                        String categorieClient, LocalDate dateInscription) {
}
