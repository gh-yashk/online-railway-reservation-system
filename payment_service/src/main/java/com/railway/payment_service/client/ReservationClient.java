package com.railway.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.railway.payment_service.dto.ReservationStatus;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "reservation-service", path = "/api/reservations")
public interface ReservationClient {

    @PutMapping("/{reservationId}/status")
    void updateReservationStatus(@PathVariable String reservationId, @RequestBody ReservationStatus status);

}
