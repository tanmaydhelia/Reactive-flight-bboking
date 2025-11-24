package com.flightspring.service;

import com.flightspring.dto.BookingRequest;
import com.flightspring.dto.CancelResponse;
import com.flightspring.dto.ItineraryDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {
	Mono<ItineraryDto> bookItinerary(String outwardFlightId, BookingRequest req);
	
	Mono<ItineraryDto> getItineraryByPnr(String pnr);
	
	Flux<ItineraryDto> getHistoryByEmail(String email);
	
	Mono<CancelResponse> cancelByPnr(String pnr);
}
