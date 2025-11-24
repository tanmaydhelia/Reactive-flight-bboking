package com.flightspring.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Document
@Data
public class Itinerary {
	@Id
	private Integer id;
	@NotBlank
	private String pnr;
	@NotNull
	private Integer userId; 
	@NotNull
	@PastOrPresent
	private LocalDateTime createdTime;
	@NotNull
	@Positive
	private int totalAmount;
	@NotNull
	private BookingStatus status;
	
}
