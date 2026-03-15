package com.railway.payment_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.railway.payment_service.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUsername(String username);

    Optional<Payment> findByReservationId(String reservationId);

    Optional<Payment> findByCheckoutSessionId(String checkoutSessionId);

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

}