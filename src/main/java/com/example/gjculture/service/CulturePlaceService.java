package com.example.gjculture.service;

import com.example.gjculture.dto.CulturePlaceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public class CulturePlaceService {

    private final WebClient webClient;
    private final String baseUrl;
    private final String serviceKey;

    public CulturePlaceService(WebClient webClient,
                                @Value("${app.grtc.base-url}") String baseUrl,
                                @Value("${app.grtc.service-key}") String serviceKey) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
    }

    public Mono<List<CulturePlaceDto>> getPlacesNearStation(String stationName) {
        // 실제 파라미터와 엔드포인트는 공사 문서/공공데이터포털의 '광주교통공사_역 인근 문화공간' 항목을 확인하여 맞춰야 합니다.
        String endpoint = baseUrl + "/openapi/culturePlaces"; // 예시

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/openapi/culturePlaces")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("stationName", stationName)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(jsonStr -> {
                    // 간단화: 실제로는 JSON 파싱 -> DTO 매핑 필요 (Jackson)
                    // 여기선 자리표시로 빈 리스트 반환
                    return List.<CulturePlaceDto>of();
                });
    }
}
