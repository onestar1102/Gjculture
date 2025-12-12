package com.example.gjculture.service;

import com.example.gjculture.dto.CulturePlaceDto;
import com.example.gjculture.dto.GrtcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CulturePlaceService {

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String baseUrl;
    private final String serviceKey;

    public CulturePlaceService(
            WebClient webClient,
            ObjectMapper mapper,
            @Value("${app.grtc.base-url}") String baseUrl,
            @Value("${app.grtc.service-key}") String serviceKey
    ) {
        this.webClient = webClient;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
    }

    public Mono<List<CulturePlaceDto>> getPlaces(String station, int page, int size) {

        String url = baseUrl
                + "?serviceKey=" + serviceKey
                + "&stationName=" + station
                + "&pageNo=" + page
                + "&numOfRows=" + size
                + "&format=json";

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        GrtcResponse res = mapper.readValue(json, GrtcResponse.class);
                        return res.body.items.item.stream()
                                .map(this::toDto)
                                .toList();
                    } catch (Exception e) {
                        throw new RuntimeException("공공데이터 파싱 실패", e);
                    }
                });
    }

    private CulturePlaceDto toDto(GrtcResponse.Item it) {
        CulturePlaceDto dto = new CulturePlaceDto();
        dto.placeName = it.placeName;
        dto.category = it.category;
        dto.locplc = it.locplc;
        dto.latitude = parse(it.latitude);
        dto.longitude = parse(it.longitude);
        return dto;
    }

    private Double parse(String v) {
        try {
            return Double.parseDouble(v);
        } catch (Exception e) {
            return null;
        }
    }
}
