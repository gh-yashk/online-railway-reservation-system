package com.railway.reservation_service.dto;

import com.railway.reservation_service.model.ReservationStatus;

public record PnrStatusResponse(
        String pnr,
        Integer trainNumber,
        ReservationStatus status,
        Integer nPassengers) {

}
