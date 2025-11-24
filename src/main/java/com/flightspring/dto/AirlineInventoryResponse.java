package com.flightspring.dto;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirlineInventoryResponse {
	private String airLineCode;
	private Integer flightsAdded;
	private List<String> flightIds;
}
