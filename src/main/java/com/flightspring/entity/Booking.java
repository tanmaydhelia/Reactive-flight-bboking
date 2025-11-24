package com.flightspring.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Document
@Data
public class Booking {
	@Id
	private Integer id;
	@NotNull
	private Integer itineraryId; // flattened FK
	@NotNull
	private Integer flightId; // flattened FK
	@NotNull
	private LocalDate journeyDate;
	@NotNull
	private TripSegmentType segmentType;
	@NotNull
	private BookingStatus status;
}
