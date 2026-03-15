package com.railway.train_service.dto;

import com.railway.train_service.model.TrainClass;

public record FareResponse(
        Integer trainNumber,
        TrainClass trainClass,
        Double fare) {

}
