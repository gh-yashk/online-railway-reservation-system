package com.railway.reservation_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.railway.reservation_service.dto.AvailableSeatsResponse;
import com.railway.reservation_service.dto.FareResponse;
import com.railway.reservation_service.model.TrainClass;

@FeignClient(name = "train-service", path = "/api/trains")
public interface TrainClient {

    @GetMapping("/{trainNumber}/fare")
    FareResponse getFare(@PathVariable Integer trainNumber, @RequestParam TrainClass trainClass);

    @GetMapping("/{trainNumber}/available-seats")
    AvailableSeatsResponse getAvailableSeats(@PathVariable Integer trainNumber);

    @PutMapping("/{trainNumber}/available-seats")
    void updateAvailableSeats(@PathVariable Integer trainNumber, @RequestParam Integer seatsToUpdate);

}
