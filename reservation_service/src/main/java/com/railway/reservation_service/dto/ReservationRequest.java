package com.railway.reservation_service.dto;

import java.util.List;

import com.railway.reservation_service.model.TrainClass;

public record ReservationRequest(
    Integer trainNumber,
    TrainClass trainClass,
    List<PassengerDto> passengers
) {

}
