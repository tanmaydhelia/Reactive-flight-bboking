package com.flightspring.dto;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.flightspring.entity.TripType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
	
	@NotBlank
	private String from;
	
	@NotBlank
	private String to;
	
	@NotNull
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDate journeyDate;
	
	@NotNull
	private TripType tripType;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDate returnDate;

}
