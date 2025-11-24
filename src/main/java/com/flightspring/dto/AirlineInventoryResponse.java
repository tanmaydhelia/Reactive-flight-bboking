package com.flightspring.dto;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirlineInventoryResponse {
	private String airLineCode;
	private Integer flightsAdded;
	private List<String> flightIds;
}
