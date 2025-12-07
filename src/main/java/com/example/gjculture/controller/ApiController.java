package com.example.gjculture.controller;

import com.example.gjculture.dto.CulturePlaceDto;
import com.example.gjculture.service.CulturePlaceService;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<List<CulturePlaceDto>>> getNearby(
            @RequestParam String station,
            @RequestParam(defaultValue = "1") int page
    ) {
        int pageSize = 10;
        int skip = (page - 1) * pageSize;

        return service.getPlacesNearStation(station)
                .map(all -> all.stream()
                        .skip(skip)
                        .limit(pageSize)
                        .toList())
                .map(ResponseEntity::ok);
    }
}
