package com.flightspring.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightspring.entity.Flight;
import com.flightspring.entity.FlightStatus;

import reactor.core.publisher.Flux;

public interface FlightRepository extends ReactiveMongoRepository<Flight, Integer>{
	Flux<Flight> findByFromAirportAndToAirportAndDepartureTimeBetweenAndStatus(
			String fromAirport,
			String toAirport,
			LocalDateTime departureStart,
			LocalDateTime departureEnd,
			FlightStatus status
		);
}
