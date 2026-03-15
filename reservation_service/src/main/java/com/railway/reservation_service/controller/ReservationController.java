package com.railway.reservation_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.reservation_service.dto.PnrStatusResponse;
import com.railway.reservation_service.dto.ReservationRequest;
import com.railway.reservation_service.model.Reservation;
import com.railway.reservation_service.model.ReservationStatus;
import com.railway.reservation_service.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Management API", description = "Endpoints for handling reservations and cancellations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create a reservation", description = "Create a new reservation for a train (requires PASSENGER role)")
    public ResponseEntity<Reservation> verifyUserAndCreateReservation(
            @RequestHeader("Authorization") String token, @RequestBody ReservationRequest request) {
        log.info("Received request to create reservation for train: {}", request.trainNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.verifyUserAndCreateReservation(token, request));
    }

    @GetMapping("/user")
    @Operation(summary = "Get user reservations", description = "Get all reservations belonging to the authenticated user")
    public ResponseEntity<List<Reservation>> verifyUserAndGetReservations(
            @RequestHeader("Authorization") String token) {
        log.info("Received request to fetch user reservations");
        return ResponseEntity.ok(reservationService.verifyUserAndGetReservations(token));
    }

    @GetMapping("/pnr/{pnr}/status")
    @Operation(summary = "Get reservation status", description = "Get the current status of a reservation by PNR")
    public ResponseEntity<PnrStatusResponse> getReservationStatusByPNR(@PathVariable String pnr) {
        log.info("Received request to check reservation status for PNR: {}", pnr);
        return ResponseEntity.ok(reservationService.getReservationStatusByPNR(pnr));
    }

    @PutMapping("/pnr/{pnr}/cancel")
    @Operation(summary = "Cancel a reservation", description = "Cancel an existing reservation by PNR")
    public ResponseEntity<Reservation> verifyUserAndCancelReservation(@RequestHeader("Authorization") String token,
            @PathVariable String pnr) {
        log.info("Received request to cancel reservation with PNR: {}", pnr);
        return ResponseEntity.ok(reservationService.verifyUserAndCancelReservation(pnr, token));
    }

    @PutMapping("/{reservationId}/status")
    public ResponseEntity<Reservation> updateReservationStatus(@PathVariable String reservationId, @RequestBody ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(reservationId, status));
    }

}
