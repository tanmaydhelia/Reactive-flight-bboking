package com.flightspring.dto;



import com.flightspring.entity.Gender;
import com.flightspring.entity.MealType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
