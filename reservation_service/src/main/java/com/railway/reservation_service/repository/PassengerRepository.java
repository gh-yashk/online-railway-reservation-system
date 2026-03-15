package com.railway.reservation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.railway.reservation_service.model.Passenger;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

}
