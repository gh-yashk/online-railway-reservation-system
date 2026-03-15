package com.railway.payment_service.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_reservation_id", columnList = "reservationId"),
        @Index(name = "idx_checkout_session_id", columnList = "checkoutSessionId"),
        @Index(name = "idx_payment_intent_id", columnList = "paymentIntentId"),
        @Index(name = "idx_username", columnList = "username")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false, unique = true)
    private String reservationId; // PNR

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * Stripe Checkout Session ID (cs_xxx)
     */
    @Column(nullable = false, unique = true)
    private String checkoutSessionId;

    /**
     * Stripe PaymentIntent ID (pi_xxx)
     * Stored after webhook confirmation
     */
    @Column(unique = true)
    private String paymentIntentId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
