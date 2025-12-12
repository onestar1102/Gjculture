package com.example.gjculture.controller;

import com.example.gjculture.dto.CulturePlaceDto;
import com.example.gjculture.service.CulturePlaceService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class ApiController {

    private final CulturePlaceService service;

    public ApiController(CulturePlaceService service) {
        this.service = service;
    }

    @GetMapping("/nearby")
    public Mono<List<CulturePlaceDto>> nearby(
            @RequestParam String station,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getPlaces(station, page, size);
    }
}
