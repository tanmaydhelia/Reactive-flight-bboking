package com.flightspring.dto;

import java.util.List;


import com.flightspring.entity.TripType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

	@NotBlank
	private String name;
	
	@NotBlank
	@Email
	private String email;
	
	@NotNull
	private TripType tripType;
	
	private String returnFlightId;
	
	@NotNull
	@Positive
	private Integer numberOfSeats;
	
	@NotEmpty
	@Valid
	private List<PassengerRequest> passengers;

}
