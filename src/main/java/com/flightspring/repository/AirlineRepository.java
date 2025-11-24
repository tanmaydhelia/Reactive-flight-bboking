package com.flightspring.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightspring.entity.Airline;

import reactor.core.publisher.Mono;

public interface AirlineRepository extends ReactiveMongoRepository<Airline, Integer>{
	
	Mono<Airline> findByCode(String code);
    Mono<Boolean> existsByCode(String code);
}
