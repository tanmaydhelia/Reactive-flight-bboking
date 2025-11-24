package com.flightspring.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightspring.entity.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, Integer>{
	Mono<User> findByEmail(String email);
	
	Mono<Boolean> existsByEmail(String email);
}
