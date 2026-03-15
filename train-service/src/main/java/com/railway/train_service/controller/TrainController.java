package com.railway.train_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railway.train_service.dto.AddTrainRequest;
import com.railway.train_service.dto.AvailableSeatsResponse;
import com.railway.train_service.dto.FareResponse;
import com.railway.train_service.model.Train;
import com.railway.train_service.model.TrainClass;
import com.railway.train_service.service.TrainService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/trains")
@RequiredArgsConstructor
@Tag(name = "Train Management API", description = "Endpoints for managing trains, checking availability, and searching routes")
public class TrainController {

    private final TrainService trainService;

    @PostMapping
    @Operation(summary = "Add a new train", description = "Add a new train (requires ADMIN role)")
    public ResponseEntity<Train> verifyUserAndAddTrain(@RequestHeader("Authorization") String token,
            @RequestBody AddTrainRequest request) {
        log.info("Received request to add train: {} by admin", request.trainNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(trainService.verifyUserAndAddTrain(token, request));
    }

    @GetMapping("/search")
    @Operation(summary = "Search for trains", description = "Search for trains between a source and destination")
    public ResponseEntity<List<Train>> searchTrains(@RequestParam String source, @RequestParam String destination) {
        log.info("Searching for trains from {} to {}", source, destination);
        return ResponseEntity.ok(trainService.searchTrains(source, destination));
    }

    @GetMapping("/{trainNumber}")
    @Operation(summary = "Get train details", description = "Retrieve a train by its number")
    public ResponseEntity<Train> getTrainByTrainNumber(@PathVariable Integer trainNumber) {
        log.info("Fetching train details for train number: {}", trainNumber);
        return ResponseEntity.ok(trainService.getTrainByTrainNumber(trainNumber));
    }

    @GetMapping("/{trainNumber}/fare")
    @Operation(summary = "Get train fare", description = "Get fare details for a specific train and class")
    public ResponseEntity<FareResponse> getFare(@PathVariable Integer trainNumber,
            @RequestParam TrainClass trainClass) {
        log.info("Fetching fare for train number: {} in class: {}", trainNumber, trainClass);
        return ResponseEntity.ok(trainService.getFare(trainNumber, trainClass));
    }

    @GetMapping("/{trainNumber}/available-seats")
    @Operation(summary = "Get available seats", description = "Check available seats for a specific train")
    public ResponseEntity<AvailableSeatsResponse> getAvailableSeats(@PathVariable Integer trainNumber) {
        log.info("Checking available seats for train number: {}", trainNumber);
        return ResponseEntity.ok(trainService.getAvailableSeats(trainNumber));
    }

    @PutMapping("/{trainNumber}/available-seats")
    @Operation(summary = "Update available seats", description = "Update the number of available seats (used by reservation service)")
    public ResponseEntity<Train> updateAvailableSeats(@PathVariable Integer trainNumber,
            @RequestParam Integer seatsToUpdate) {
        log.info("Received request to update available seats for train: {} by {}", trainNumber, seatsToUpdate);
        return ResponseEntity.ok(trainService.updateAvailableSeats(trainNumber, seatsToUpdate));
    }

}
