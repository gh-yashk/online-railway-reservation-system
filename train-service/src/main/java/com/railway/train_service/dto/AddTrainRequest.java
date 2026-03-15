package com.railway.train_service.dto;

import jakarta.validation.constraints.NotNull;

public record AddTrainRequest(
        @NotNull Integer trainNumber,
        @NotNull String name,
        @NotNull String source,
        @NotNull String destination,
        @NotNull Integer totalDistanceInKm,
        @NotNull Integer totalSeats) {

}
