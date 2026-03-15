package com.railway.train_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.railway.train_service.model.Train;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(Integer trainNumber);

    List<Train> findBySourceAndDestination(String source, String destination);

}
