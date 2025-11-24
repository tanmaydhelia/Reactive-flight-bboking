package com.flightspring.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.flightspring.entity.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarySummary {
	private String pnr;
	private BookingStatus status;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime createdTime;
	
	private int totalAmount;
	private String routeSummary;
	private int totalPassengers;
}
