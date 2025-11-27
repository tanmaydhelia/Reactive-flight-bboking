package com.flightspring.service.implimentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightspring.dto.AirlineInventoryRequest;
import com.flightspring.dto.FlightInventoryItemDto;
import com.flightspring.entity.Airline;
import com.flightspring.entity.Flight;
import com.flightspring.exception.ResourceNotFoundException;
import com.flightspring.repository.AirlineRepository;
import com.flightspring.repository.FlightRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void testAddInventory_Success() {
        // Data
        AirlineInventoryRequest req = new AirlineInventoryRequest();
        req.setAirlineCode("IND");
        
        FlightInventoryItemDto item = new FlightInventoryItemDto();
        item.setFromAirport("DEL");
        item.setToAirport("BLR");
        item.setDepartureTime(LocalDateTime.now().plusDays(1));
        item.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        item.setPrice(5000);
        item.setTotalSeats(100);
        req.setFlights(List.of(item));

        Airline airline = new Airline();
        airline.setId("airline-1");
        airline.setCode("IND");

        Flight savedFlight = new Flight();
        savedFlight.setId("flight-1");
        savedFlight.setAirlineId("airline-1");

        when(airlineRepository.findByCode("IND")).thenReturn(Mono.just(airline));
        when(flightRepository.saveAll(any(List.class))).thenReturn(Flux.just(savedFlight));

        StepVerifier.create(adminService.addInventory(req))
            .assertNext(res -> {
                Assertions.assertEquals("IND", res.getAirLineCode());
                Assertions.assertEquals(1, res.getFlightsAdded());
                Assertions.assertEquals("flight-1", res.getFlightIds().get(0));
            })
            .verifyComplete();
    }

    @Test
    void testAddInventory_AirlineNotFound() {
        AirlineInventoryRequest req = new AirlineInventoryRequest();
        req.setAirlineCode("UNKNOWN");
        req.setFlights(List.of());

        when(airlineRepository.findByCode("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(adminService.addInventory(req))
            .expectError(ResourceNotFoundException.class)
            .verify();
    }

    @Test
    void testValidateFlight_InvalidDates() {
        AirlineInventoryRequest req = new AirlineInventoryRequest();
        req.setAirlineCode("IND");
        
        FlightInventoryItemDto item = new FlightInventoryItemDto();
        // Departure AFTER Arrival (Invalid)
        item.setDepartureTime(LocalDateTime.now().plusDays(2));
        item.setArrivalTime(LocalDateTime.now().plusDays(1));
        item.setTotalSeats(100);
        req.setFlights(List.of(item));

        Airline airline = new Airline();
        airline.setId("airline-1");

        when(airlineRepository.findByCode("IND")).thenReturn(Mono.just(airline));

        StepVerifier.create(adminService.addInventory(req))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException 
                && throwable.getMessage().contains("Arrival must be after departure"))
            .verify();
    }
}