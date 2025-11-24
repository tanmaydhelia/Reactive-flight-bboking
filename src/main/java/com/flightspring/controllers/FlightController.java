package com.flightspring.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightspring.dto.BookingRequest;
import com.flightspring.dto.CancelResponse;
import com.flightspring.dto.FlightSearchRequest;
import com.flightspring.dto.FlightSummaryDto;
import com.flightspring.dto.ItineraryDto;
import com.flightspring.service.BookingService;
import com.flightspring.service.FlightService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api/flight")
public class FlightController {
	
	private final FlightService flightService;
	private final BookingService bookingService;
	
	public FlightController(FlightService flightService, BookingService bookingService) {
        this.flightService = flightService;
        this.bookingService = bookingService;
    }
	
	@PostMapping("/search")
	public Flux<FlightSummaryDto> searchFlights(@RequestBody @Valid FlightSearchRequest req){
		log.info("POST /api/v1.0/flight/search from={} to={} date={} tripType={}", req.getFrom(), req.getTo(), req.getJourneyDate(), req.getTripType());
		return flightService.searchFlights(req);
	}
	
	@PostMapping("/booking/{flightId}")
	public Mono<ItineraryDto> bookTicket(@PathVariable String flightId, @RequestBody @Valid BookingRequest req) {
		log.info("POST /api/v1.0/flight/booking/{} for email={} tripType={}",
                flightId, req.getEmail(), req.getTripType());
		return bookingService.bookItinerary(flightId, req);
	}
	
	@GetMapping("/ticket/{pnr}")
	public Mono<ItineraryDto> getTicketByPnr(@PathVariable String pnr) {
		log.info("GET /api/v1.0/flight/ticket/{}",pnr);
		return bookingService.getItineraryByPnr(pnr);
	}
	
	@GetMapping("/booking/history/{emailId}")
	public Flux<ItineraryDto> getBookinghistory(@PathVariable String emailId){
		log.info("GET /api/v1.0/flight/booking/history/{}",emailId);
		return bookingService.getHistoryByEmail(emailId);
	}
	
	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<CancelResponse> cancelBooking(@PathVariable String pnr) {
		log.info("DELETE /api/v1.0/flight/booking/cancel/{}",pnr);
		return bookingService.cancelByPnr(pnr);
	}
}
