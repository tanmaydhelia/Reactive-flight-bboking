package com.flightspring.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightspring.dto.AirlineInventoryRequest;
import com.flightspring.dto.AirlineInventoryResponse;
import com.flightspring.dto.FlightInventoryItemDto;
import com.flightspring.service.AdminService;


import reactor.core.publisher.Mono;

@WebFluxTest(AdminControllerTest.class)
public class AdminControllerTest {
	@Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private AdminService adminService;
    
    @Test
    void testAddInventory() {
        
        AirlineInventoryRequest req = new AirlineInventoryRequest();
        req.setAirlineCode("IND");
       
        FlightInventoryItemDto item = new FlightInventoryItemDto();
        item.setFromAirport("DEL");
        item.setToAirport("BLR");
       
        req.setFlights(List.of(item)); 

        AirlineInventoryResponse response = new AirlineInventoryResponse();
        response.setAirLineCode("IND");
        response.setFlightsAdded(1);

        when(adminService.addInventory(any(AirlineInventoryRequest.class)))
            .thenReturn(Mono.just(response));

    
        webTestClient.post()
            .uri("/api/flight/airline/inventory/add")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk()
            .expectBody(AirlineInventoryResponse.class)
            .value(res -> {
                Assertions.assertEquals("IND", res.getAirLineCode());
                Assertions.assertEquals(1, res.getFlightsAdded());
            });
    }

}

