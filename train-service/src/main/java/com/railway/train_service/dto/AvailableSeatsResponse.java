package com.railway.train_service.dto;

public record AvailableSeatsResponse(
        Integer trainNumber,
        Integer availableSeats) {

}
