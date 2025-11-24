package com.flightspring.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Document
@Data
public class Flight {
	@Id
	private Integer id;
	@NotNull
	private Integer airlineId; // flattened FK
	@NotBlank
	private String fromAirport;
	@NotBlank
	private String toAirport;
	@NotNull
	private LocalDateTime departureTime;
	@NotNull
	private LocalDateTime arrivalTime;
	@NotNull
	@Positive
	private int price;
	@NotNull
	@Positive
	private int totalSeats;
	@NotNull
	@Min(0)
	private int availableSeats;
	@NotNull
	private FlightStatus status;

}
