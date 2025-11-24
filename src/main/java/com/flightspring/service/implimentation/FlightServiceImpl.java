package com.flightspring.service.implimentation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.flightspring.dto.FlightSearchRequest;
import com.flightspring.dto.FlightSummaryDto;
import com.flightspring.entity.Flight;
import com.flightspring.entity.FlightStatus;
import com.flightspring.repository.AirlineRepository;
import com.flightspring.repository.FlightRepository;
import com.flightspring.service.FlightService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class FlightServiceImpl implements FlightService{
	 
	private final AirlineRepository airlineRepository;
	private final FlightRepository flightRepository;
    public FlightServiceImpl(AirlineRepository airlineRepository, FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
        this.airlineRepository = airlineRepository;
    }

	@Override
    public Flux<FlightSummaryDto> searchFlights(FlightSearchRequest req) {
        log.info("Searching flights from={} to={} date={} tripType={}", req.getFrom(), req.getTo(), req.getJourneyDate(), req.getTripType());
        LocalDate journeyDate = req.getJourneyDate();
        LocalDateTime start = journeyDate.atStartOfDay();
        LocalDateTime end = journeyDate.plusDays(1).atStartOfDay();

        return flightRepository
            .findByFromAirportAndToAirportAndDepartureTimeBetweenAndStatus(req.getFrom(), req.getTo(), start, end, FlightStatus.SCHEDULED)
            .flatMap(flight -> 
                airlineRepository.findById(flight.getAirlineId())
                    .map(airline -> {
                        FlightSummaryDto f = new FlightSummaryDto();
                        f.setFlightId(flight.getId());
                        f.setAirlineName(airline.getName());
                        f.setAirlineCode(airline.getCode());
                        f.setFromAirport(flight.getFromAirport());
                        f.setToAirport(flight.getToAirport());
                        f.setDepartureTime(flight.getDepartureTime());
                        f.setArrivalTime(flight.getArrivalTime());
                        f.setPrice(flight.getPrice());
                        return f;
                    })
                    .defaultIfEmpty(toFlightSummaryDtoWithoutAirline(flight))
            );
    }

    private FlightSummaryDto toFlightSummaryDtoWithoutAirline(Flight flight) {
        FlightSummaryDto f = new FlightSummaryDto();
        f.setFlightId(flight.getId());
        f.setAirlineName("Unknown");
        f.setAirlineCode("N/A");
        f.setFromAirport(flight.getFromAirport());
        f.setToAirport(flight.getToAirport());
        f.setDepartureTime(flight.getDepartureTime());
        f.setArrivalTime(flight.getArrivalTime());
        f.setPrice(flight.getPrice());
        return f;
    }
}
