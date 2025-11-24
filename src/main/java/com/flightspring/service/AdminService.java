package com.flightspring.service;

import com.flightspring.dto.AirlineInventoryRequest;
import com.flightspring.dto.AirlineInventoryResponse;

import reactor.core.publisher.Mono;

public interface AdminService {
	Mono<AirlineInventoryResponse> addInventory (AirlineInventoryRequest request);
}
