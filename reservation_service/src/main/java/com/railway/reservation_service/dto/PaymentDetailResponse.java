package com.railway.reservation_service.dto;

public record PaymentDetailResponse(
        String reservationId,
        String username,
        String currency,
        Double amount,
        String status,
        String transactionId,
        String paymentUrl) {

}
