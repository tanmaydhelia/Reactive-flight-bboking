package com.flightspring.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightspring.entity.Booking;

import reactor.core.publisher.Flux;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String>{
	Flux<Booking> findByItineraryId(String itineraryId);
}
