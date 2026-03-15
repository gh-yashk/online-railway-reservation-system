package com.railway.payment_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.payment_service.client.ReservationClient;
import com.railway.payment_service.dto.PaymentDetailResponse;
import com.railway.payment_service.dto.PaymentRequest;
import com.railway.payment_service.dto.ReservationStatus;
import com.railway.payment_service.exception.DuplicateResourceException;
import com.railway.payment_service.exception.PaymentException;
import com.railway.payment_service.exception.ResourceNotFoundException;
import com.railway.payment_service.model.Payment;
import com.railway.payment_service.model.PaymentStatus;
import com.railway.payment_service.repository.PaymentRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final ReservationClient reservationClient;

        public PaymentDetailResponse getPaymentDetail(String checkoutSessionId) {

                Payment payment = paymentRepository
                                .findByCheckoutSessionId(checkoutSessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

                return new PaymentDetailResponse(payment.getReservationId(),
                                payment.getUsername(),
                                payment.getCurrency(),
                                payment.getAmount(),
                                payment.getStatus(),
                                payment.getCheckoutSessionId(),
                                null);
        }

        @Transactional
        @SuppressWarnings("null")
        public PaymentDetailResponse makePayment(PaymentRequest request) {

                try {

                        log.info("Creating Stripe Checkout session for reservation {}", request.reservationId());

                        SessionCreateParams params = SessionCreateParams.builder()
                                        .setMode(SessionCreateParams.Mode.PAYMENT)
                                        .setSuccessUrl("http://localhost:8084/api/payments/success?session_id={CHECKOUT_SESSION_ID}")
                                        .setCancelUrl("http://localhost:8084/api/payments/cancel")
                                        .putMetadata("reservationId", request.reservationId())
                                        .addLineItem(SessionCreateParams.LineItem.builder()
                                                        .setQuantity(1L)
                                                        .setPriceData(SessionCreateParams.LineItem.PriceData
                                                                        .builder()
                                                                        .setCurrency(request.currency())
                                                                        .setUnitAmount((long) (request.amount() * 100))
                                                                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData
                                                                                        .builder()
                                                                                        .setName("Train Booking PNR: "
                                                                                                        + request.reservationId())
                                                                                        .build())
                                                                        .build())
                                                        .build())
                                        .build();

                        Session session = Session.create(params);

                        Payment payment = Payment.builder()
                                        .reservationId(request.reservationId())
                                        .username(request.username())
                                        .currency(request.currency())
                                        .amount(request.amount())
                                        .checkoutSessionId(session.getId())
                                        .status(PaymentStatus.PENDING)
                                        .build();

                        paymentRepository.save(payment);

                        log.info("Stripe Checkout session created {}", session.getId());

                        return new PaymentDetailResponse(
                                        payment.getReservationId(),
                                        payment.getUsername(),
                                        payment.getCurrency(),
                                        payment.getAmount(),
                                        payment.getStatus(),
                                        payment.getCheckoutSessionId(),
                                        session.getUrl());

                } catch (StripeException e) {
                        throw new PaymentException("Stripe payment failed: " + e.getMessage());
                }
        }

        @Transactional
        public PaymentDetailResponse refundPayment(String reservationId) {

                Payment payment = paymentRepository
                                .findByReservationId(reservationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Payment not found for reservation ID: " + reservationId));

                if (payment.getStatus() == PaymentStatus.REFUNDED) {
                        throw new DuplicateResourceException("Payment already refunded");
                }

                if (payment.getStatus() != PaymentStatus.SUCCESS) {
                        throw new PaymentException("Only successful payments can be refunded");
                }

                try {

                        RefundCreateParams params = RefundCreateParams.builder()
                                        .setPaymentIntent(payment.getPaymentIntentId())
                                        .build();

                        Refund refund = Refund.create(params);

                        log.info("Stripe refund successful {}", refund.getId());

                        payment.setStatus(PaymentStatus.REFUNDED);
                        paymentRepository.save(payment);

                        updateReservationStatus(payment.getReservationId(), PaymentStatus.REFUNDED);

                        return new PaymentDetailResponse(
                                        payment.getReservationId(),
                                        payment.getUsername(),
                                        payment.getCurrency(),
                                        payment.getAmount(),
                                        payment.getStatus(),
                                        payment.getCheckoutSessionId(),
                                        null);

                } catch (StripeException e) {
                        throw new PaymentException("Stripe refund failed: " + e.getMessage());
                }
        }

        @Transactional
        public void handleStripeEvent(Event event) {

                log.info("Received Stripe event {}", event.getType());

                StripeObject stripeObject;

                try {
                        stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
                } catch (EventDataObjectDeserializationException e) {
                        log.error("Webhook deserialization failed {}", e.getMessage());
                        return;
                }

                switch (event.getType()) {

                        case "checkout.session.completed":

                                Session completedSession = (Session) stripeObject;

                                updatePaymentSuccess(
                                                completedSession.getId(),
                                                completedSession.getPaymentIntent());

                                break;

                        case "checkout.session.expired":

                                Session expiredSession = (Session) stripeObject;

                                updatePaymentStatus(expiredSession.getId(), PaymentStatus.FAILED);

                                break;

                        default:
                                log.info("Unhandled Stripe event {}", event.getType());
                }
        }

        @Transactional
        public void updatePaymentSuccess(String sessionId, String paymentIntentId) {

                Payment payment = paymentRepository
                                .findByCheckoutSessionId(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

                if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        log.info("Webhook already processed for {}", sessionId);
                        return;
                }

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaymentIntentId(paymentIntentId);

                paymentRepository.save(payment);

                log.info("Payment {} marked SUCCESS", sessionId);

                updateReservationStatus(payment.getReservationId(), PaymentStatus.SUCCESS);
        }

        @Transactional
        public void updatePaymentStatus(String sessionId, PaymentStatus status) {

                Payment payment = paymentRepository
                                .findByCheckoutSessionId(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

                if (payment.getStatus() == status) {
                        return;
                }

                payment.setStatus(status);
                paymentRepository.save(payment);

                log.info("Payment {} updated to {}", sessionId, status);

                updateReservationStatus(payment.getReservationId(), status);
        }

        private void updateReservationStatus(String reservationId, PaymentStatus status) {

                ReservationStatus reservationStatus = (status == PaymentStatus.SUCCESS)
                                ? ReservationStatus.CONFIRMED
                                : ReservationStatus.CANCELLED;

                try {
                        reservationClient.updateReservationStatus(reservationId, reservationStatus);
                } catch (Exception e) {
                        log.error("Failed to update reservation {}", e.getMessage());
                }
        }
}