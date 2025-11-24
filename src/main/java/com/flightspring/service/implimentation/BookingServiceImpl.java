package com.flightspring.service.implimentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.flightspring.dto.BookingRequest;
import com.flightspring.dto.CancelResponse;
import com.flightspring.dto.ItineraryDto;
import com.flightspring.dto.LegDto;
import com.flightspring.dto.PassengerDto;
import com.flightspring.dto.PassengerRequest;
import com.flightspring.entity.Booking;
import com.flightspring.entity.BookingStatus;
import com.flightspring.entity.Flight;
import com.flightspring.entity.Itinerary;
import com.flightspring.entity.Passenger;
import com.flightspring.entity.Role;
import com.flightspring.entity.TripSegmentType;
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
import com.flightspring.service.BookingService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
	private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final ItineraryRepository itineraryRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;

    public BookingServiceImpl(UserRepository userRepository,FlightRepository flightRepository,ItineraryRepository itineraryRepository,BookingRepository bookingRepository,PassengerRepository passengerRepository) {
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
        this.itineraryRepository = itineraryRepository;
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
    }
    
    
	@Override
    public Mono<ItineraryDto> bookItinerary(String outwardFlightId, BookingRequest req) {
        validateBookingReq(req);

        Mono<User> userMono = getOrCreateUser(req.getName(), req.getEmail());
        Mono<Flight> outwardFlightMono = getFlightOrThrow(outwardFlightId);
        
        Mono<Optional<Flight>> returnFlightMono = (req.getTripType() == TripType.ROUND_TRIP && req.getReturnFlightId() != null)
                ? getFlightOrThrow(req.getReturnFlightId()).map(Optional::of)
                : Mono.just(Optional.empty());

        return Mono.zip(userMono, outwardFlightMono, returnFlightMono)
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    Flight outwardFlight = tuple.getT2();
                    Flight returnFlight = tuple.getT3().orElse(null);
                    boolean isRoundTrip = (returnFlight != null);

                    int seats = req.getNumberOfSeats();

                    if (outwardFlight.getAvailableSeats() < seats) {
                        return Mono.<ItineraryDto>error(new SeatNotAvailableException("Not enough seats available on outward flight"));
                    }
                    if (isRoundTrip && returnFlight.getAvailableSeats() < seats) {
                        return Mono.<ItineraryDto>error(new SeatNotAvailableException("Not enough seats available on return flight"));
                    }

                    if (hasDuplicateSeatNumbers(req.getPassengers())) {
                        return Mono.<ItineraryDto>error(new SeatNotAvailableException("Duplicate seat numbers in request"));
                    }

                    int outwardAmount = outwardFlight.getPrice() * seats;
                    int returnAmount = isRoundTrip ? returnFlight.getPrice() * seats : 0;
                    int totalAmount = outwardAmount + returnAmount;

                    outwardFlight.setAvailableSeats(outwardFlight.getAvailableSeats() - seats);
                    Mono<Flight> savedOutwardMono = flightRepository.save(outwardFlight);
                    
                    Mono<Optional<Flight>> savedReturnMono = isRoundTrip
                            ? flightRepository.save(applySeatDecrement(returnFlight, seats)).map(Optional::of)
                            : Mono.just(Optional.empty());

                    return Mono.zip(savedOutwardMono, savedReturnMono)
                            .flatMap(flightsTuple -> {
                                Flight savedOutward = flightsTuple.getT1();
                                Flight savedReturn = flightsTuple.getT2().orElse(null);

                                Itinerary itinerary = new Itinerary();
                                itinerary.setPnr(generatePnr());
                                itinerary.setUserId(user.getId());
                                itinerary.setCreatedTime(LocalDateTime.now());
                                itinerary.setTotalAmount(totalAmount);
                                itinerary.setStatus(BookingStatus.BOOKED);

                                return itineraryRepository.save(itinerary)
                                        .flatMap(savedItinerary -> createBookingsAndDto(
                                                savedItinerary,
                                                user,
                                                savedOutward,
                                                savedReturn,
                                                isRoundTrip,
                                                req.getPassengers()
                                        ));
                            });
                });
    }
	private Mono<ItineraryDto> createBookingsAndDto(Itinerary itinerary,User user,Flight outwardFlight,Flight returnFlight,boolean isRoundTrip,List<PassengerRequest> passengerRequests){
		Mono<Booking> outwardBookingMono = createBookingWithPassengers(
				itinerary, outwardFlight,
				isRoundTrip ? TripSegmentType.OUTBOUND : TripSegmentType.ONE_WAY,
						passengerRequests
				);

		if (!isRoundTrip || returnFlight == null) {
			return outwardBookingMono.then(buildItineraryDto(itinerary, user));
		} else {
			Mono<Booking> returnBookingMono = outwardBookingMono.then(createBookingWithPassengers(itinerary, returnFlight,TripSegmentType.RETURN,passengerRequests));
			return returnBookingMono.then(buildItineraryDto(itinerary, user));
		}
	}

	private Mono<Booking> createBookingWithPassengers(
			Itinerary itinerary,
			Flight flight,
			TripSegmentType segmentType,
			List<PassengerRequest> passengerRequests
			) {
		Booking booking = new Booking();
		booking.setItineraryId(itinerary.getId());
		booking.setFlightId(flight.getId());
		booking.setJourneyDate(flight.getDepartureTime().toLocalDate());
		booking.setSegmentType(segmentType);
		booking.setStatus(BookingStatus.BOOKED);

		return bookingRepository.save(booking)
				.flatMap(savedBooking -> {
					List<Passenger> passengers = buildPassengersForBooking(savedBooking.getId(), passengerRequests);
					return passengerRepository.saveAll(passengers)
							.then(Mono.just(savedBooking));
				});
	}

	private List<Passenger> buildPassengersForBooking(String bookingId, List<PassengerRequest> passengerRequests) {
		List<Passenger> passengers = new ArrayList<>();
		for (PassengerRequest pr : passengerRequests) {
			Passenger p = new Passenger();
			p.setBookingId(bookingId);
			p.setName(pr.getName());
			p.setGender(pr.getGender());
			p.setAge(pr.getAge());
			p.setMealType(pr.getMealType());
			p.setSeatNumber(pr.getSeatNumber());
			passengers.add(p);
		}
		return passengers;
	}

	private boolean hasDuplicateSeatNumbers(List<PassengerRequest> passengerRequests) {
		Set<String> seatNumbers = new HashSet<>();
		for (PassengerRequest pr : passengerRequests) {
			if (!seatNumbers.add(pr.getSeatNumber())) {
				return true;
			}
		}
		return false;
	}

	private Flight applySeatDecrement(Flight flight, int seats) {
		flight.setAvailableSeats(flight.getAvailableSeats() - seats);
		return flight;
	}

	@Override
	public Mono<ItineraryDto> getItineraryByPnr(String pnr) {
		log.info("Fetching itinerary by PNR={} (reactive)", pnr);
		return itineraryRepository.findByPnr(pnr)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("No itinerary for pnr!!!")))
				.flatMap(itinerary ->
				userRepository.findById(itinerary.getUserId())
				.flatMap(user -> buildItineraryDto(itinerary, user))
						);
	}

	@Override
	public Flux<ItineraryDto> getHistoryByEmail(String email) {
		log.info("Fetching booking history for email={} (reactive)", email);
		return userRepository.findByEmail(email)
				.flatMapMany(user ->
				itineraryRepository.findByUserId(user.getId())
				.flatMap(itinerary -> buildItineraryDto(itinerary, user))
						);
	}

	@Override
	public Mono<CancelResponse> cancelByPnr(String pnr) {
		log.info("Attempting Cancel (reactive) for pnr = {}", pnr);
		LocalDateTime now = LocalDateTime.now();

		return itineraryRepository.findByPnr(pnr)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Itinerary not found for PNR: " + pnr)))
				.flatMap(itinerary ->
				bookingRepository.findByItineraryId(itinerary.getId())
				.collectList()
				.flatMap(bookings -> checkCancellationAllowed(bookings, now)
						.then(updateSeatsAndCancelBookings(itinerary, bookings, pnr))
						)
						);
	}

	private Mono<Void> checkCancellationAllowed(List<Booking> bookings, LocalDateTime now) {
        return Flux.fromIterable(bookings)
            .filter(b -> b.getStatus() == BookingStatus.BOOKED)
            .flatMap(b -> flightRepository.findById(b.getFlightId()))
            .flatMap(flight -> {
                if (now.plusHours(24).isAfter(flight.getDepartureTime())) {
                    return Mono.error(new CancellationNotAllowedException("Cannot Cancel as booking within 24hrs"));
                }
                return Mono.empty();
            })
            .then();
    }

	private Mono<CancelResponse> updateSeatsAndCancelBookings(
			Itinerary itinerary,
			List<Booking> bookings,
			String pnr
			) {
		return Flux.fromIterable(bookings)
				.filter(b -> b.getStatus() == BookingStatus.BOOKED)
				.flatMap(booking ->
				passengerRepository.findByBookingId(booking.getId())
				.count()
				.flatMap(passengerCount ->
				flightRepository.findById(booking.getFlightId())
				.flatMap(flight -> {
					flight.setAvailableSeats(flight.getAvailableSeats() + passengerCount.intValue());
					return flightRepository.save(flight);
				})
						)
				.then(Mono.defer(() -> {
					booking.setStatus(BookingStatus.CANCELLED);
					return bookingRepository.save(booking);
				}))
						)
				.collectList()
				.then(Mono.defer(() -> {
					itinerary.setStatus(BookingStatus.CANCELLED);
					return itineraryRepository.save(itinerary);
				}))
				.map(saved -> {
					CancelResponse cr = new CancelResponse();
					cr.setPnr(pnr);
					cr.setStatus(BookingStatus.CANCELLED);
					cr.setMessage("Booking Cancelled Successfully!!!");
					return cr;
				});
	}

	private void validateBookingReq(BookingRequest req) {
		if (req.getPassengers() == null || req.getPassengers().isEmpty()) {
			throw new IllegalArgumentException("Add Atleast one passenger!!!");
		}
		if (req.getNumberOfSeats() == 0) {
			throw new IllegalArgumentException("No Seats requested!!!");
		}
		if (req.getPassengers().size() != req.getNumberOfSeats()) {
			throw new IllegalArgumentException("Number of seats != number of passengers!!!");
		}
	}

	private Mono<User> getOrCreateUser(String name, String email) {
		return userRepository.findByEmail(email)
				.switchIfEmpty(Mono.defer(() -> {
					log.info("User Not found for email={}, creating new user", email);
					User u = new User();
					u.setName(name);
					u.setEmail(email);
					u.setRole(Role.USER);
					return userRepository.save(u);
				}));
	}

	private Mono<Flight> getFlightOrThrow(String flightId) {
		return flightRepository.findById(flightId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Flight not found for id=" + flightId)));
	}

	private String generatePnr() {
		String uuid = UUID.randomUUID().toString().replace("-", " ").toUpperCase();
		return "TAD" + uuid.substring(0, 5).replace(" ", "");
	}

	private Mono<ItineraryDto> buildItineraryDto(Itinerary itinerary, User user) {
		ItineraryDto dto = new ItineraryDto();
		dto.setPnr(itinerary.getPnr());
		dto.setUserName(user.getName());
		dto.setEmail(user.getEmail());
		dto.setStatus(itinerary.getStatus());
		dto.setTotalAmount(itinerary.getTotalAmount());
		dto.setCreatedTime(itinerary.getCreatedTime());

		return bookingRepository.findByItineraryId(itinerary.getId())
				.flatMap(booking -> flightRepository.findById(booking.getFlightId())
						.flatMap(flight ->  passengerRepository.findByBookingId(booking.getId())
								.collectList()
								.map(passengers -> {
									LegDto leg = new LegDto();
									leg.setBookingId(booking.getId());
									leg.setFlightId(flight.getId());
									leg.setFromAirport(flight.getFromAirport());
									leg.setToAirport(flight.getToAirport());
									leg.setDepartureTime(flight.getDepartureTime());
									leg.setArrivalTime(flight.getArrivalTime());
									leg.setSegmentType(booking.getSegmentType());
									leg.setStatus(booking.getStatus());
									leg.setPassengers(toPassengerDtoList(passengers));
									return leg;
								})
								)
						)
				.collectList()
				.map(legs -> {
					dto.setLegs(legs);
					return dto;
				});
	}

	private List<PassengerDto> toPassengerDtoList(List<Passenger> passengers) {
		List<PassengerDto> result = new ArrayList<>();
		for (Passenger p : passengers) {
			result.add(toPassengerDto(p));
		}
		return result;
	}

	private PassengerDto toPassengerDto(Passenger pass) {
		PassengerDto pd = new PassengerDto();
		pd.setName(pass.getName());
		pd.setGender(pass.getGender());
		pd.setAge(pass.getAge());
		pd.setMealType(pass.getMealType());
		pd.setSeatNumber(pass.getSeatNumber());
		return pd;
	}
}
