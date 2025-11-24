package com.flightspring.dto;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightInventoryItemDto {

	@NotBlank
	private String fromAirport;
	
	@NotBlank
	private String toAirport;
	
	@NotNull
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime departureTime;
	
	@NotNull
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime arrivalTime;
	
	@NotNull
	@Positive
	private Integer price;
	
	@NotNull
	@Positive
	private Integer totalSeats;
	
	@NotNull
	@Min(0)
	private Integer availabeSeats;

}
