package com.railway.reservation_service.dto;

public record PaymentRequest(
        String reservationId,
        String username,
        String currency,
        Double amount) {

}
