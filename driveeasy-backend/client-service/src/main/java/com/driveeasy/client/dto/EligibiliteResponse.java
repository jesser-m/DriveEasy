package com.driveeasy.client.dto;

import java.util.List;

public record EligibiliteResponse(Long clientId, boolean eligible, int age, boolean permisValide, List<String> motifs) {
}
