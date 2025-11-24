package com.flightspring.entity;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Document
@Data
public class Passenger {
	@Id
	private Integer id;
	@NotNull
	private Integer bookingId; 
	@NotBlank
	private String name;
	@NotNull
	private Gender gender;
	@NotNull
	@Range(min = 0, max = 100)
	private int age;
	@NotBlank
	private String seatNumber;
	@NotNull
	private MealType mealType;

}
