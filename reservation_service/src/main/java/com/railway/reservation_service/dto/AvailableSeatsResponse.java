package com.railway.reservation_service.dto;

public record AvailableSeatsResponse(
        Integer trainNumber,
        Integer availableSeats) {

}
