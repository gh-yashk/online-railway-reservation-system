package com.railway.payment_service.dto;

import com.railway.payment_service.model.PaymentStatus;

public record PaymentDetailResponse(
        String reservationId,
        String username,
        String currency,
        Double amount,
        PaymentStatus status,
        String transactionId,
        String paymentUrl) {

}
