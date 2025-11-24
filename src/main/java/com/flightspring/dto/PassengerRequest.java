package com.flightspring.dto;


import org.hibernate.validator.constraints.Range;
import org.springframework.data.mongodb.core.mapping.Document;

import com.flightspring.entity.Gender;
import com.flightspring.entity.MealType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerRequest {

	@NotBlank
	private String name;
	
	@NotNull
	private Gender gender;
	
	@NotNull
	@Range(min=0,max=100)
	private Integer age;
	
	@NotNull
	private MealType mealType;
	
	@NotBlank
	private String seatNumber;

}
