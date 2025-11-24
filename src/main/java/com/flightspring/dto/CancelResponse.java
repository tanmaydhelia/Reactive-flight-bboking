package com.flightspring.dto;

import org.springframework.data.mongodb.core.mapping.Document;

import com.flightspring.entity.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelResponse {
	private String pnr;
	private BookingStatus status;
	private String message;
}
