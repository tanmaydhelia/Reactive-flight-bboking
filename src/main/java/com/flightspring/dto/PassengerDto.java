package com.flightspring.dto;


import org.springframework.data.mongodb.core.mapping.Document;

import com.flightspring.entity.Gender;
import com.flightspring.entity.MealType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDto {
	private String name;
	private Gender gender;
	private Integer age;
	private MealType mealType;
	private String seatNumber;
}
