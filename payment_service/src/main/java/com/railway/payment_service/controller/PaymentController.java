package com.railway.payment_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.payment_service.dto.PaymentDetailResponse;
import com.railway.payment_service.dto.PaymentRequest;
import com.railway.payment_service.model.PaymentStatus;
import com.railway.payment_service.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management API", description = "Endpoints for handling payments and refunds using Stripe")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get payment details", description = "Retrieve a payment by its transaction ID")
    public ResponseEntity<PaymentDetailResponse> getPaymentDetail(@PathVariable String transactionId) {
        log.info("Received request to fetch payment details for transaction ID: {}", transactionId);
        return ResponseEntity.ok(paymentService.getPaymentDetail(transactionId));
    }

    @PostMapping
    @Operation(summary = "Make a payment", description = "Process a new payment using Stripe")
    public ResponseEntity<PaymentDetailResponse> makePayment(@RequestBody PaymentRequest request) {
        log.info("Received request to make payment for reservation ID: {}", request.reservationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.makePayment(request));
    }

    @PostMapping("/reservation/{reservationId}/refund")
    @Operation(summary = "Refund a payment", description = "Refund a payment by checking Stripe payment intent via reservation ID")
    public ResponseEntity<PaymentDetailResponse> refundPayment(@PathVariable String reservationId) {
        log.info("Received request from reservation service to refund payment for reservation ID: {}", reservationId);
        return ResponseEntity.ok(paymentService.refundPayment(reservationId));
    }

    @GetMapping("/success")
    public ResponseEntity<PaymentStatus> success() {
        return ResponseEntity.ok(PaymentStatus.SUCCESS);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Handle cancelled payment", description = "Endpoint redirected to by Stripe when a payment is cancelled")
    public ResponseEntity<PaymentStatus> handlePaymentCancel() {
        log.info("Payment was cancelled by the user");
        return ResponseEntity.ok(PaymentStatus.FAILED);
    }

}
