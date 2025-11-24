package com.flightspring.service.implimentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.flightspring.dto.AirlineInventoryRequest;
import com.flightspring.dto.AirlineInventoryResponse;
import com.flightspring.dto.FlightInventoryItemDto;
import com.flightspring.entity.Flight;
import com.flightspring.entity.FlightStatus;
import com.flightspring.exception.ResourceNotFoundException;
import com.flightspring.repository.AirlineRepository;
import com.flightspring.repository.FlightRepository;
import com.flightspring.service.AdminService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService{
	
    private final AirlineRepository airlineRepository;
    private final FlightRepository flightRepository;
    public AdminServiceImpl(AirlineRepository airlineRepository, FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
        this.airlineRepository = airlineRepository;
    }

    @Override
    public Mono<AirlineInventoryResponse> addInventory(AirlineInventoryRequest request) {
        String airlineCode = request.getAirlineCode();
        log.info("Adding inventory for airlineCode={} with {} flights", airlineCode, request.getFlights().size());
        
        return airlineRepository.findByCode(airlineCode)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Airline not found with Code: " + airlineCode)))
            .flatMap(airline -> {
                List<Flight> flightsToPersist = new ArrayList<>();
                for(FlightInventoryItemDto item : request.getFlights()) {
                    validateFlightInventoryItem(item);
                    Flight flight = new Flight();
                    flight.setAirlineId(airline.getId());
                    flight.setFromAirport(item.getFromAirport());
                    flight.setToAirport(item.getToAirport());
                    flight.setDepartureTime(item.getDepartureTime());
                    flight.setArrivalTime(item.getArrivalTime());
                    flight.setPrice(item.getPrice());
                    flight.setTotalSeats(item.getTotalSeats());
                    flight.setAvailableSeats(item.getTotalSeats());
                    flight.setStatus(FlightStatus.SCHEDULED);
                    flightsToPersist.add(flight);
                }
                
                return flightRepository.saveAll(flightsToPersist)
                    .collectList()
                    .map(savedFlights -> {
                        log.info("Successfully added {} flights for airlineCode {}", savedFlights.size(), airlineCode);
                        AirlineInventoryResponse res = new AirlineInventoryResponse();
                        res.setAirLineCode(airlineCode);
                        res.setFlightsAdded(savedFlights.size());
                        List<String> ids = savedFlights.stream()
                            .map(Flight::getId)
                            .collect(Collectors.toList());
                        res.setFlightIds(ids);
                        return res;
                    });
            });
    }

    private void validateFlightInventoryItem(FlightInventoryItemDto item) {
        LocalDateTime dep = item.getDepartureTime();
        LocalDateTime arr = item.getArrivalTime();
        
        if(dep == null || arr == null) {
            throw new IllegalArgumentException("Equal Arrival and Dept Time...");
        }
        if(!arr.isAfter(dep)) {
            throw new IllegalArgumentException("Arrival must be after departure...");
        }
        if(item.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Total Seats is less than zero...");
        }
    }
}
