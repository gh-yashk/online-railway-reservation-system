package com.railway.reservation_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.railway.reservation_service.dto.PaymentDetailResponse;
import com.railway.reservation_service.dto.PaymentRequest;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @PostMapping("/api/payments")
    PaymentDetailResponse makePayment(@RequestBody PaymentRequest request);

    @PostMapping("/api/payments/reservation/{reservationId}/refund")
    PaymentDetailResponse refundPayment(@PathVariable("reservationId") String reservationId);
}
