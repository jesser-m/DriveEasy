package com.driveeasy.client.dto;

import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;
import java.time.LocalDate;

public record ClientResponse(Long id, String cin, String nom, String prenom, LocalDate dateNaissance, String email,
                             String telephone, String adresse, String numeroPermis, LocalDate dateExpirationPermis,
                             CategorieClient categorieClient, LocalDate dateInscription) {

    public static ClientResponse from(Client c) {
        return new ClientResponse(c.getId(), c.getCin(), c.getNom(), c.getPrenom(), c.getDateNaissance(), c.getEmail(),
                c.getTelephone(), c.getAdresse(), c.getNumeroPermis(), c.getDateExpirationPermis(),
                c.getCategorieClient(), c.getDateInscription());
    }
}
