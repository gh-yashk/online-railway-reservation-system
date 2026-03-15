package com.railway.train_service.model;

import lombok.Getter;

@Getter
public enum TrainClass {

    SLEEPER(0.5),
    AC3(1),
    AC2(1.2),
    AC1(2.0);

    private final double farePerKm;

    TrainClass(double farePerKm) {
        this.farePerKm = farePerKm;
    }

}
