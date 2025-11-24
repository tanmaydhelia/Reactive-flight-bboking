package com.flightspring.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightspring.entity.Passenger;

import reactor.core.publisher.Flux;

public interface PassengerRepository extends ReactiveMongoRepository<Passenger, Integer>{
	Flux<Passenger> findByBookingId(Integer bookingId);
}
