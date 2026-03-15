package com.railway.reservation_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.railway.reservation_service.client.AuthClient;
import com.railway.reservation_service.client.PaymentClient;
import com.railway.reservation_service.client.TrainClient;
import com.railway.reservation_service.dto.AvailableSeatsResponse;
import com.railway.reservation_service.dto.PaymentDetailResponse;
import com.railway.reservation_service.dto.PaymentRequest;
import com.railway.reservation_service.dto.PnrStatusResponse;
import com.railway.reservation_service.dto.ReservationRequest;
import com.railway.reservation_service.dto.ValidationResponse;
import com.railway.reservation_service.model.Passenger;
import com.railway.reservation_service.model.Reservation;
import com.railway.reservation_service.model.ReservationStatus;
import com.railway.reservation_service.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final AuthClient authClient;

    private final TrainClient trainClient;

    private final PaymentClient paymentClient;

    @SuppressWarnings("null")
    public Reservation verifyUserAndCreateReservation(String token, ReservationRequest request) {
        log.info("Starting reservation creation process for train: {}", request.trainNumber());
        String username = verifyUserAndCheckIsPassenger(token);

        log.debug("Checking seat availability for {} passengers on train {}", request.passengers().size(),
                request.trainNumber());
        AvailableSeatsResponse availableSeatsResponse = trainClient.getAvailableSeats(request.trainNumber());
        if (request.passengers().size() > availableSeatsResponse.availableSeats()) {
            log.warn("Not enough available seats for train: {}", request.trainNumber());
            throw new RuntimeException("Not enough available seats for train number: " + request.trainNumber());
        }

        double fare = trainClient.getFare(request.trainNumber(), request.trainClass()).fare();
        log.debug("Calculated total fare: {}", fare);

        List<Passenger> passengers = new ArrayList<>();
        request.passengers().forEach(p -> passengers.add(Passenger.builder().name(p.name()).build()));

        String generatedPnr = "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Generated PNR: {} mapping to user: {}", generatedPnr, username);

        Reservation reservation = Reservation.builder()
                .pnr(generatedPnr)
                .username(username)
                .trainNumber(request.trainNumber())
                .trainClass(request.trainClass())
                .totalFare(fare)
                .status(ReservationStatus.PENDING)
                .passengers(passengers)
                .build();

        passengers.forEach(p -> p.setReservation(reservation));

        Reservation savedReservation = reservationRepository.save(reservation);

        log.debug("Updating train {} seat count by -{}", request.trainNumber(), request.passengers().size());
        trainClient.updateAvailableSeats(request.trainNumber(), -request.passengers().size());

        try {
            log.info("Initiating payment through Feign Client for PNR: {} amount: {}", savedReservation.getPnr(), fare);
            PaymentRequest paymentRequest = new PaymentRequest(
                    savedReservation.getPnr(),
                    username,
                    "inr",
                    fare);
            PaymentDetailResponse paymentRes = paymentClient.makePayment(paymentRequest);
            log.info("Payment initiated, redirect URL generated for PNR: {}", savedReservation.getPnr());
            savedReservation.setPaymentUrl(paymentRes.paymentUrl());
            savedReservation.setStatus(ReservationStatus.PENDING);
        } catch (Exception e) {
            log.error("Payment failed for PNR {}: {}. Rolling back reserved seats.", savedReservation.getPnr(),
                    e.getMessage());
            trainClient.updateAvailableSeats(request.trainNumber(), request.passengers().size());
            savedReservation.setStatus(ReservationStatus.CANCELLED);
        }

        return reservationRepository.save(savedReservation);
    }

    public List<Reservation> verifyUserAndGetReservations(String token) {
        log.info("Fetching reservations for validated user");
        String username = verifyUserAndCheckIsPassenger(token);
        return reservationRepository.findByUsername(username);
    }

    public PnrStatusResponse getReservationStatusByPNR(String pnr) {
        log.info("Fetching status for PNR: {}", pnr);
        Reservation reservation = reservationRepository.findByPnr(pnr).orElseThrow(() -> {
            log.warn("Status fetch failed - PNR not found: {}", pnr);
            return new RuntimeException("PNR not found");
        });
        return new PnrStatusResponse(
                reservation.getPnr(),
                reservation.getTrainNumber(),
                reservation.getStatus(),
                reservation.getPassengers().size());
    }

    public Reservation verifyUserAndCancelReservation(String pnr, String token) {
        log.info("Initiating cancellation process for PNR: {}", pnr);
        String username = verifyUserAndCheckIsPassenger(token);

        Reservation reservation = reservationRepository.findByPnr(pnr).orElseThrow(() -> {
            log.warn("Cancel attempt failed - PNR not found: {}", pnr);
            return new RuntimeException("PNR not found");
        });

        if (!username.equals(reservation.getUsername())) {
            log.error("Unauthorized cancellation attempt on PNR {} by user {}", pnr, username);
            throw new RuntimeException("Unauthorized to cancel this reservation");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            try {
                log.info("Sending refund request to payment client for PNR: {}", reservation.getPnr());
                paymentClient.refundPayment(reservation.getPnr());

                log.debug("Restoring seat availability for train {}", reservation.getTrainNumber());
                trainClient.updateAvailableSeats(reservation.getTrainNumber(), reservation.getPassengers().size());
            } catch (Exception e) {
                log.error("Error processing refund for PNR {}: {}", reservation.getPnr(), e.getMessage());
            }
        }

        log.info("Marking reservation PNR {} as CANCELLED", pnr);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    private String verifyUserAndCheckIsPassenger(String token) {
        try {
            log.debug("Validating token and passenger role");
            ValidationResponse validationResponse = authClient.validateToken(token);
            if (!validationResponse.valid() || !validationResponse.role().contains("PASSENGER")) {
                log.warn("Token validation failed or missing PASSENGER role");
                throw new RuntimeException("Unauthorized: Invalid token or insufficient permissions");
            }
            return validationResponse.username();
        } catch (Exception e) {
            log.error("Auth client verification failed: {}", e.getMessage());
            throw new RuntimeException("Unauthorized: " + e.getMessage());
        }
    }

    public Reservation updateReservationStatus(String pnr, ReservationStatus status) {
        log.info("Updating reservation status for reservation ID / PNR: {} to {}", pnr, status);
        Reservation reservation = reservationRepository.findByPnr(pnr).orElseThrow(() -> {
            log.warn("Reservation status update failed - reservation ID not found: {}", pnr);
            return new RuntimeException("Reservation not found");
        });
        reservation.setStatus(status);

        if (status == ReservationStatus.CANCELLED) {
            trainClient.updateAvailableSeats(reservation.getTrainNumber(), reservation.getPassengers().size());
        }

        return reservationRepository.save(reservation);
    }

}
