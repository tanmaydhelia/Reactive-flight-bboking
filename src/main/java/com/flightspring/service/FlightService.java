package com.flightspring.service;

import com.flightspring.dto.FlightSearchRequest;
import com.flightspring.dto.FlightSummaryDto;

import reactor.core.publisher.Flux;

public interface FlightService {
	Flux<FlightSummaryDto> searchFlights(FlightSearchRequest req);

}
