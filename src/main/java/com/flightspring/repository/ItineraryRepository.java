package com.flightspring.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightspring.entity.Itinerary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItineraryRepository extends ReactiveMongoRepository<Itinerary, String>{
	Mono<Itinerary> findByPnr(String pnr);
	
	Flux<Itinerary> findByUserId(String userId);
}
