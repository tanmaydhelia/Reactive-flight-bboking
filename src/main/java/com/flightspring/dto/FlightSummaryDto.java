package com.flightspring.dto;


import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSummaryDto {
	private String flightId;
	private String airlineName;
	private String airlineCode;
	private String fromAirport;
	private String toAirport;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime departureTime;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime arrivalTime;
	
	private Integer price;
}
