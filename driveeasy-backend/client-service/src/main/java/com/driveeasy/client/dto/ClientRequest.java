package com.driveeasy.client.dto;

import com.driveeasy.client.model.CategorieClient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record ClientRequest(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "le CIN doit contenir 8 chiffres") String cin,
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotNull @Past LocalDate dateNaissance,
        @NotBlank @Email String email,
        String telephone,
        String adresse,
        @NotBlank String numeroPermis,
        @NotNull LocalDate dateExpirationPermis,
        CategorieClient categorieClient) {
}
