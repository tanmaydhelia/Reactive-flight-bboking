package com.flightspring.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Document
@Data
public class User {
	@Id
	private Integer id;
	@NotBlank
	private String name;
	@NotBlank
	@Email
	private String email;
	@NotNull
	private Role role;
}
