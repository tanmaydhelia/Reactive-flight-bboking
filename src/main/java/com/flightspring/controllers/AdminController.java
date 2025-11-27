package com.flightspring.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightspring.dto.AirlineInventoryRequest;
import com.flightspring.dto.AirlineInventoryResponse;
import com.flightspring.service.AdminService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api/flight/airline")
public class AdminController {

	private final AdminService adminService;
	
	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}
	
	@PostMapping("inventory/add")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<AirlineInventoryResponse> addInventory(@RequestBody @Valid AirlineInventoryRequest req) {
		log.info("POST /api/v1.0/flight/airline/inventory/add for airlineCode={} flights={}", req.getAirlineCode(), req.getFlights()!=null?req.getFlights().size():0);
		return adminService.addInventory(req);
	}
}
