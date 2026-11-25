package com.driveeasy.client.dto;

import com.driveeasy.client.model.CategorieClient;
import jakarta.validation.constraints.NotNull;

public record CategorieRequest(@NotNull CategorieClient categorieClient) {
}
