package com.flightspring.dto;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.flightspring.entity.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDto {
	private String pnr;
	private String userName;
	private String email;
	private BookingStatus status;
	private int totalAmount;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime createdTime;
	
	private List<LegDto> legs;

}
