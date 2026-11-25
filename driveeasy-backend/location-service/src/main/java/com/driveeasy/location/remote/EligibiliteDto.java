package com.driveeasy.location.remote;

import java.util.List;

public record EligibiliteDto(Long clientId, boolean eligible, int age, boolean permisValide, List<String> motifs) {
}
