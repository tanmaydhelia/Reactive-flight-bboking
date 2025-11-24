package com.flightspring.dto;


import com.flightspring.entity.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelResponse {
	private String pnr;
	private BookingStatus status;
	private String message;
}
