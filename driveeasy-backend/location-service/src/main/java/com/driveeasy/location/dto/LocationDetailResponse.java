package com.driveeasy.location.dto;

import com.driveeasy.location.remote.ClientDto;
import com.driveeasy.location.remote.VehiculeDto;

/** Detail enrichi : location + client + vehicule agreges (null si le service distant est indisponible). */
public record LocationDetailResponse(LocationResponse location, ClientDto client, VehiculeDto vehicule) {
}
