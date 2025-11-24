package com.flightspring.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Document
@Data
public class Airline {
	
	@Id
	private Integer id;
	
	@NotBlank
	private String name;
	
	@NotBlank
	@Indexed(unique=true)
	private String code;
}
