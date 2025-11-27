package com.flightspring.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightspring.dto.BookingRequest;
import com.flightspring.dto.CancelResponse;
import com.flightspring.dto.FlightSearchRequest;
import com.flightspring.dto.FlightSummaryDto;
import com.flightspring.dto.ItineraryDto;
import com.flightspring.dto.PassengerRequest;
import com.flightspring.entity.BookingStatus;
import com.flightspring.entity.Gender;
import com.flightspring.entity.MealType;
import com.flightspring.entity.TripType;
import com.flightspring.service.BookingService;
import com.flightspring.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(FlightController.class)
public class FlightControllerTest {
	@Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FlightService flightService;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void testSearchFlights() {
        FlightSearchRequest req = new FlightSearchRequest();
        req.setFrom("DEL");
        req.setTo("BLR");
        req.setJourneyDate(LocalDate.now().plusDays(1));
        req.setTripType(TripType.ONE_WAY);

        FlightSummaryDto summary = new FlightSummaryDto();
        summary.setFlightId("1");
        summary.setAirlineCode("IND");

        when(flightService.searchFlights(any(FlightSearchRequest.class)))
            .thenReturn(Flux.just(summary));

        webTestClient.post()
            .uri("/api/flight/search")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated()
            .expectBodyList(FlightSummaryDto.class)
            .hasSize(1);
    }

    @Test
    void testBookTicket() {
        String flightId = "101";
        BookingRequest req = new BookingRequest();
        req.setEmail("test@test.com");
        req.setName("Test User");
        req.setTripType(TripType.ONE_WAY);
        req.setNumberOfSeats(1);
        PassengerRequest p = new PassengerRequest();
        p.setName("John Doe");
        p.setGender(Gender.MALE);
        p.setAge(30);
        p.setMealType(MealType.VEG);
        p.setSeatNumber("1A");
        req.setPassengers(List.of(p));

        ItineraryDto itinerary = new ItineraryDto();
        itinerary.setPnr("PNR123");
        itinerary.setStatus(BookingStatus.BOOKED);

        when(bookingService.bookItinerary(eq(flightId), any(BookingRequest.class)))
            .thenReturn(Mono.just(itinerary));

        webTestClient.post()
            .uri("/api/flight/booking/{flightId}", flightId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ItineraryDto.class)
            .value(res -> Assertions.assertEquals("PNR123", res.getPnr()));
    }

    @Test
    void testGetTicketByPnr() {
        String pnr = "PNR123";
        ItineraryDto itinerary = new ItineraryDto();
        itinerary.setPnr(pnr);

        when(bookingService.getItineraryByPnr(pnr))
            .thenReturn(Mono.just(itinerary));

        webTestClient.get()
            .uri("/api/flight/ticket/{pnr}", pnr)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ItineraryDto.class)
            .value(res -> Assertions.assertEquals(pnr, res.getPnr()));
    }

    @Test
    void testGetBookingHistory() {
        String email = "test@test.com";
        ItineraryDto i1 = new ItineraryDto();
        ItineraryDto i2 = new ItineraryDto();

        when(bookingService.getHistoryByEmail(email))
            .thenReturn(Flux.just(i1, i2));

        webTestClient.get()
            .uri("/api/flight/booking/history/{emailId}", email)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(ItineraryDto.class)
            .hasSize(2);
    }

    @Test
    void testCancelBooking() {
        String pnr = "PNR123";
        CancelResponse response = new CancelResponse();
        response.setPnr(pnr);
        response.setStatus(BookingStatus.CANCELLED);

        when(bookingService.cancelByPnr(pnr))
            .thenReturn(Mono.just(response));

        webTestClient.delete()
            .uri("/api/flight/booking/cancel/{pnr}", pnr)
            .exchange()
            .expectStatus().isOk()
            .expectBody(CancelResponse.class)
            .value(res -> Assertions.assertEquals(BookingStatus.CANCELLED, res.getStatus()));
    }
}
