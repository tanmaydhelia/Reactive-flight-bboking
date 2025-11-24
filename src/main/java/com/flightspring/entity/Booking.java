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
	private String id;
	@NotNull
	private String itineraryId; // flattened FK
	@NotNull
	private String flightId; // flattened FK
	@NotNull
	private LocalDate journeyDate;
	@NotNull
	private TripSegmentType segmentType;
	@NotNull
	private BookingStatus status;
}
