package com.flightspring.dto;

import java.util.List;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirlineInventoryRequest {
	@NotBlank
	private String airlineCode;
	
	@NotEmpty
	private List<FlightInventoryItemDto> flights;
}
