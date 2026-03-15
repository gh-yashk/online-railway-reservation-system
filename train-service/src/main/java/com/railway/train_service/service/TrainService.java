package com.railway.train_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.railway.train_service.client.AuthClient;
import com.railway.train_service.dto.AddTrainRequest;
import com.railway.train_service.dto.AvailableSeatsResponse;
import com.railway.train_service.dto.FareResponse;
import com.railway.train_service.dto.ValidationResponse;
import com.railway.train_service.model.Train;
import com.railway.train_service.model.TrainClass;
import com.railway.train_service.repository.TrainRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainRepository trainRepository;

    private final AuthClient authClient;

    @SuppressWarnings("null")
    public Train verifyUserAndAddTrain(String token, AddTrainRequest request) {

        try {
            log.info("Validating admin token for adding train");
            ValidationResponse validationResponse = authClient.validateToken(token);
            if (!validationResponse.valid() || !validationResponse.role().contains("ADMIN")) {
                log.warn("Unauthorized attempt to add train by user without ADMIN role");
                throw new RuntimeException("Unauthorized: Invalid token or insufficient permissions");
            }
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Unauthorized: " + e.getMessage());
        }

        return trainRepository.save(
                Train.builder()
                        .trainNumber(request.trainNumber())
                        .name(request.name())
                        .source(request.source())
                        .destination(request.destination())
                        .totalDistanceInKm(request.totalDistanceInKm())
                        .totalSeats(request.totalSeats())
                        .build());
    }

    public List<Train> searchTrains(String source, String destination) {
        log.info("Querying trains between {} and {}", source, destination);
        return trainRepository.findBySourceAndDestination(source, destination);
    }

    public Train getTrainByTrainNumber(Integer trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber)
                .orElseThrow(() -> {
                    log.error("Train not found with number: {}", trainNumber);
                    return new RuntimeException("Train not found with number: " + trainNumber);
                });
    }

    public FareResponse getFare(Integer trainNumber, TrainClass trainClass) {
        Train train = getTrainByTrainNumber(trainNumber);
        double fare = train.getTotalDistanceInKm() * trainClass.getFarePerKm();
        return new FareResponse(trainNumber, trainClass, fare);
    }

    public Train updateAvailableSeats(Integer trainNumber, Integer seatsToUpdate) {
        Train train = getTrainByTrainNumber(trainNumber);
        int updatedSeats = train.getAvailableSeats() + seatsToUpdate;
        if (updatedSeats < 0 || updatedSeats > train.getTotalSeats()) {
            log.error("Invalid seats update for train {}. Attempted to set {} seats.", trainNumber, updatedSeats);
            throw new IllegalArgumentException(
                    "Invalid seats update. Available seats cannot be negative or exceed total seats.");
        }
        train.setAvailableSeats(updatedSeats);
        log.info("Updated available seats for train: {} to {}", trainNumber, updatedSeats);
        return trainRepository.save(train);
    }

    public AvailableSeatsResponse getAvailableSeats(Integer trainNumber) {
        Train train = getTrainByTrainNumber(trainNumber);
        return new AvailableSeatsResponse(trainNumber, train.getAvailableSeats());
    }

}
