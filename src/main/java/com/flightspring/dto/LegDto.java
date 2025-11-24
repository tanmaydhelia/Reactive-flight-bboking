package com.flightspring.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.flightspring.entity.BookingStatus;
import com.flightspring.entity.TripSegmentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegDto {
	private String bookingId;
	private String flightId;
	private String fromAirport;
	private String toAirport;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime departureTime;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime arrivalTime;
	
	private TripSegmentType segmentType;
	private BookingStatus status;
	private List<PassengerDto> passengers;
	
	
}
