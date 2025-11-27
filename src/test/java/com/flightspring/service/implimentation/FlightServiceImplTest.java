package com.flightspring.service.implimentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightspring.dto.FlightSearchRequest;
import com.flightspring.entity.Airline;
import com.flightspring.entity.Flight;
import com.flightspring.entity.FlightStatus;
import com.flightspring.repository.AirlineRepository;
import com.flightspring.repository.FlightRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AirlineRepository airlineRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    @Test
    void testSearchFlights_Found() {
        FlightSearchRequest req = new FlightSearchRequest();
        req.setFrom("DEL");
        req.setTo("BLR");
        req.setJourneyDate(LocalDate.now());

        Flight flight = new Flight();
        flight.setId("f1");
        flight.setAirlineId("a1");
        flight.setFromAirport("DEL");
        flight.setToAirport("BLR");
        flight.setPrice(5000);
        flight.setDepartureTime(LocalDateTime.now());
        flight.setArrivalTime(LocalDateTime.now().plusHours(2));

        Airline airline = new Airline();
        airline.setId("a1");
        airline.setName("Indigo");
        airline.setCode("IND");

        when(flightRepository.findByFromAirportAndToAirportAndDepartureTimeBetweenAndStatus(
                eq("DEL"), eq("BLR"), any(LocalDateTime.class), any(LocalDateTime.class), eq(FlightStatus.SCHEDULED)))
            .thenReturn(Flux.just(flight));

        when(airlineRepository.findById("a1")).thenReturn(Mono.just(airline));

        StepVerifier.create(flightService.searchFlights(req))
            .assertNext(dto -> {
                Assertions.assertEquals("f1", dto.getFlightId());
                Assertions.assertEquals("Indigo", dto.getAirlineName());
                Assertions.assertEquals(5000, dto.getPrice());
            })
            .verifyComplete();
    }
    
    @Test
    void testSearchFlights_AirlineMissing() {
        FlightSearchRequest req = new FlightSearchRequest();
        req.setFrom("DEL");
        req.setTo("BLR");
        req.setJourneyDate(LocalDate.now());

        Flight flight = new Flight();
        flight.setId("f1");
        flight.setAirlineId("a1");
        flight.setFromAirport("DEL");
        flight.setToAirport("BLR");

        when(flightRepository.findByFromAirportAndToAirportAndDepartureTimeBetweenAndStatus(
                anyString(), anyString(), any(), any(), any()))
            .thenReturn(Flux.just(flight));

        // Airline NOT Found
        when(airlineRepository.findById("a1")).thenReturn(Mono.empty());

        StepVerifier.create(flightService.searchFlights(req))
            .assertNext(dto -> {
                Assertions.assertEquals("f1", dto.getFlightId());
                Assertions.assertEquals("Unknown", dto.getAirlineName()); // Default fallback
            })
            .verifyComplete();
    }
}