package com.railway.reservation_service.dto;

import com.railway.reservation_service.model.TrainClass;

public record FareResponse(
        Integer trainNumber,
        TrainClass trainClass,
        Double fare) {

}
