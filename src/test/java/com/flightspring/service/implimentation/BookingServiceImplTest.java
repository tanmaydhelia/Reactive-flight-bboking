package com.flightspring.service.implimentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightspring.dto.BookingRequest;
import com.flightspring.dto.PassengerRequest;
import com.flightspring.entity.Booking;
import com.flightspring.entity.BookingStatus;
import com.flightspring.entity.Flight;
import com.flightspring.entity.Itinerary;
import com.flightspring.entity.Passenger;
import com.flightspring.entity.TripType;
import com.flightspring.entity.User;
import com.flightspring.exception.CancellationNotAllowedException;
import com.flightspring.exception.ResourceNotFoundException;
import com.flightspring.exception.SeatNotAvailableException;
import com.flightspring.repository.BookingRepository;
import com.flightspring.repository.FlightRepository;
import com.flightspring.repository.ItineraryRepository;
import com.flightspring.repository.PassengerRepository;
import com.flightspring.repository.UserRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private ItineraryRepository itineraryRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PassengerRepository passengerRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;


    @Test
    void testBookItinerary_Success_OneWay() {
        // Request
        String outwardId = "101";
        BookingRequest req = new BookingRequest();
        req.setEmail("test@test.com");
        req.setName("Test User");
        req.setNumberOfSeats(1);
        req.setTripType(TripType.ONE_WAY);
        // No return flight ID needed for one-way
        
        PassengerRequest pReq = new PassengerRequest();
        pReq.setName("P1");
        pReq.setSeatNumber("1A");
        req.setPassengers(List.of(pReq));

        // Mock Entities
        User user = new User(); user.setId("u1"); user.setEmail("test@test.com");
        
        Flight outFlight = new Flight(); 
        outFlight.setId("101"); 
        outFlight.setPrice(100); 
        outFlight.setAvailableSeats(10); 
        outFlight.setDepartureTime(LocalDateTime.now().plusDays(2));

        Itinerary itinerary = new Itinerary(); itinerary.setId("i1"); itinerary.setPnr("PNR_ONEWAY");
        Booking b1 = new Booking(); b1.setId("b1"); b1.setFlightId("101");
        Passenger p1 = new Passenger(); p1.setId("pass1");

        // Mock Calls
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(user));
        when(flightRepository.findById("101")).thenReturn(Mono.just(outFlight));
        
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(Mono.just(itinerary));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(b1));
        when(passengerRepository.saveAll(any(List.class))).thenReturn(Flux.just(p1));

        // Re-fetching for DTO construction
        when(bookingRepository.findByItineraryId("i1")).thenReturn(Flux.just(b1));
        when(passengerRepository.findByBookingId(anyString())).thenReturn(Flux.just(p1));

        StepVerifier.create(bookingService.bookItinerary(outwardId, req))
            .assertNext(dto -> {
                Assertions.assertEquals("PNR_ONEWAY", dto.getPnr());
                Assertions.assertEquals(1, dto.getLegs().size()); // Should have 1 leg
            })
            .verifyComplete();
    }

    

    @Test
    void testBookItinerary_Success_RoundTrip() {
        // 1. Request Data for Round Trip
        String outwardId = "101";
        BookingRequest req = new BookingRequest();
        req.setEmail("round@test.com");
        req.setName("Round User");
        req.setNumberOfSeats(1);
        req.setTripType(TripType.ROUND_TRIP);
        req.setReturnFlightId("102"); // Critical for Round Trip coverage

        PassengerRequest pReq = new PassengerRequest();
        pReq.setName("P1");
        pReq.setSeatNumber("1A");
        req.setPassengers(List.of(pReq));

        // 2. Mock Entities
        User user = new User(); user.setId("u2"); user.setEmail("round@test.com");

        // Outward Flight
        Flight outFlight = new Flight(); 
        outFlight.setId("101"); 
        outFlight.setPrice(100); 
        outFlight.setAvailableSeats(10); 
        outFlight.setDepartureTime(LocalDateTime.now().plusDays(2));

        // Return Flight
        Flight retFlight = new Flight(); 
        retFlight.setId("102"); 
        retFlight.setPrice(150); 
        retFlight.setAvailableSeats(10); 
        retFlight.setDepartureTime(LocalDateTime.now().plusDays(5));

        Itinerary itinerary = new Itinerary(); itinerary.setId("i2"); itinerary.setPnr("PNR_RT");

        Booking b1 = new Booking(); b1.setId("b1"); b1.setFlightId("101");
        Booking b2 = new Booking(); b2.setId("b2"); b2.setFlightId("102"); // Return booking
        
        Passenger p1 = new Passenger(); p1.setId("pass1");

        // 3. Mock Calls
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(user));
        
        // Flight Lookups (Both Outward and Return)
        when(flightRepository.findById("101")).thenReturn(Mono.just(outFlight));
        when(flightRepository.findById("102")).thenReturn(Mono.just(retFlight));

        // Flight Saves (Both Outward and Return updates)
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(Mono.just(itinerary));

        // Booking Saves (Matches logic for creating two bookings)
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking booking = i.getArgument(0);
            // Simulate assigning ID based on flight to distinguish them in the flux later if needed
            if (booking.getFlightId().equals("101")) booking.setId("b1");
            else booking.setId("b2");
            return Mono.just(booking);
        });

        when(passengerRepository.saveAll(any(List.class))).thenReturn(Flux.just(p1));

        // Re-fetching for DTO construction (Must return BOTH bookings)
        when(bookingRepository.findByItineraryId("i2")).thenReturn(Flux.just(b1, b2));
        
        // Mock finding passengers for any booking ID
        when(passengerRepository.findByBookingId(anyString())).thenReturn(Flux.just(p1));

        // 4. Verify
        StepVerifier.create(bookingService.bookItinerary(outwardId, req))
            .assertNext(dto -> {
                Assertions.assertEquals("PNR_RT", dto.getPnr());
                Assertions.assertEquals(2, dto.getLegs().size()); // Assert 2 legs exist
            })
            .verifyComplete();
    }


    @Test
    void testBookItinerary_SeatNotAvailable() {
        BookingRequest req = new BookingRequest();
        req.setNumberOfSeats(5);
        req.setTripType(TripType.ONE_WAY);
        PassengerRequest p = new PassengerRequest(); p.setSeatNumber("1A");
        req.setPassengers(List.of(p, p, p, p, p)); // 5 pax

        Flight flight = new Flight();
        flight.setId("101");
        flight.setAvailableSeats(2); // Only 2 left

        when(userRepository.findByEmail(any())).thenReturn(Mono.just(new User()));
        when(flightRepository.findById("101")).thenReturn(Mono.just(flight));

        StepVerifier.create(bookingService.bookItinerary("101", req))
            .expectError(SeatNotAvailableException.class)
            .verify();
    }

    // --- GET TESTS ---

    @Test
    void testGetItineraryByPnr_Success() {
        Itinerary itinerary = new Itinerary();
        itinerary.setId("i1");
        itinerary.setUserId("u1");
        itinerary.setPnr("PNR123");

        User user = new User();
        user.setName("Test User");

        when(itineraryRepository.findByPnr("PNR123")).thenReturn(Mono.just(itinerary));
        when(userRepository.findById("u1")).thenReturn(Mono.just(user));
        // Mocking DTO build calls (empty for simplicity as DTO logic is shared)
        when(bookingRepository.findByItineraryId("i1")).thenReturn(Flux.empty());

        StepVerifier.create(bookingService.getItineraryByPnr("PNR123"))
            .assertNext(dto -> Assertions.assertEquals("PNR123", dto.getPnr()))
            .verifyComplete();
    }
    
    @Test
    void testGetItineraryByPnr_NotFound() {
        when(itineraryRepository.findByPnr("INVALID")).thenReturn(Mono.empty());
        StepVerifier.create(bookingService.getItineraryByPnr("INVALID"))
            .expectError(ResourceNotFoundException.class)
            .verify();
    }

    // --- CANCELLATION TESTS ---

    @Test
    void testCancelByPnr_Success() {
        String pnr = "PNR123";
        Itinerary itinerary = new Itinerary();
        itinerary.setId("i1");
        
        Booking booking = new Booking();
        booking.setId("b1");
        booking.setFlightId("f1");
        booking.setStatus(BookingStatus.BOOKED);
        
        Flight flight = new Flight();
        flight.setId("f1");
        flight.setDepartureTime(LocalDateTime.now().plusDays(5)); // > 24 hours
        flight.setAvailableSeats(10);

        when(itineraryRepository.findByPnr(pnr)).thenReturn(Mono.just(itinerary));
        when(bookingRepository.findByItineraryId("i1")).thenReturn(Flux.just(booking));
        when(flightRepository.findById("f1")).thenReturn(Mono.just(flight));
        
        // Count passengers to restore seats
        when(passengerRepository.findByBookingId("b1")).thenReturn(Flux.just(new Passenger(), new Passenger())); // 2 pax
        
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(Mono.just(itinerary));

        StepVerifier.create(bookingService.cancelByPnr(pnr))
            .assertNext(res -> {
                Assertions.assertEquals(BookingStatus.CANCELLED, res.getStatus());
                // Verify flight seats were restored (10 + 2 = 12)
                verify(flightRepository).save(org.mockito.ArgumentMatchers.argThat(f -> f.getAvailableSeats() == 12));
            })
            .verifyComplete();
    }

    @Test
    void testCancelByPnr_TooLate() {
        String pnr = "PNR123";
        Itinerary itinerary = new Itinerary(); itinerary.setId("i1");
        Booking booking = new Booking(); booking.setId("b1"); booking.setFlightId("f1"); booking.setStatus(BookingStatus.BOOKED);
        
        Flight flight = new Flight();
        flight.setId("f1");
        flight.setDepartureTime(LocalDateTime.now().plusHours(2)); // < 24 hours (Cannot cancel)

        when(itineraryRepository.findByPnr(pnr)).thenReturn(Mono.just(itinerary));
        when(bookingRepository.findByItineraryId("i1")).thenReturn(Flux.just(booking));
        when(flightRepository.findById("f1")).thenReturn(Mono.just(flight));

        StepVerifier.create(bookingService.cancelByPnr(pnr))
            .expectError(CancellationNotAllowedException.class)
            .verify();
    }
}