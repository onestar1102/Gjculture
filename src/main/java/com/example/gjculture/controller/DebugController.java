package com.example.gjculture.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 🔧 디버깅용 컨트롤러
 * - 엔드포인트를 자동으로 테스트
 * - 사용법: http://localhost:8080/debug/test-endpoints?station=광주역
 *
 * ⚠️ 개발용이므로 프로덕션에서는 삭제하세요!
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    private final WebClient webClient;

    @Value("${app.grtc.service-key}")
    private String serviceKey;

    public DebugController(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * ✅ 실제 API 테스트
     *
     * 접속: http://localhost:8080/debug/test-real-api?station=광주역
     */
    @GetMapping("/test-real-api")
    public Mono<String> testRealApi(
            @RequestParam(defaultValue = "광주역") String station
    ) {
        // ✅ 실제 엔드포인트
        String endpoint = "/GET_OAMS_CLTIPLCE_01";

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/6290000/gjCulturePlaceService" + endpoint)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 10)
                        .queryParam("type", "json")
                        .queryParam("stationNm", station)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    return "✅ 광주교통공사 API 응답 성공!\n\n" +
                            "엔드포인트: " + endpoint + "\n" +
                            "역명: " + station + "\n\n" +
                            "응답:\n" + response;
                })
                .onErrorResume(e -> Mono.just(
                        "❌ API 호출 실패!\n\n" +
                                "에러: " + e.getMessage() + "\n\n" +
                                "체크사항:\n" +
                                "1. application.yaml에 서비스 키가 올바르게 입력되었는지 확인\n" +
                                "2. 공공데이터포털에서 API 활용신청이 승인되었는지 확인\n" +
                                "3. 서비스 키에 특수문자나 공백이 없는지 확인"
                ));
    }

    /**
     * 특정 엔드포인트만 테스트
     *
     * 접속: http://localhost:8080/debug/test?endpoint=/getCulturePlace&station=광주역
     */
    @GetMapping("/test")
    public Mono<String> testSpecificEndpoint(
            @RequestParam String endpoint,
            @RequestParam(defaultValue = "광주역") String station
    ) {
        String url = "http://apis.data.go.kr/6290000/gjCulturePlaceService" + endpoint;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/6290000/gjCulturePlaceService" + endpoint)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("stationNm", station)
                        .queryParam("numOfRows", 10)
                        .queryParam("pageNo", 1)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    // 응답을 보기 좋게 포맷
                    return "✅ 성공!\n\n" +
                            "엔드포인트: " + endpoint + "\n" +
                            "URL: " + url + "\n\n" +
                            "응답:\n" + response;
                })
                .onErrorResume(e -> Mono.just(
                        "❌ 실패!\n\n" +
                                "엔드포인트: " + endpoint + "\n" +
                                "에러: " + e.getMessage()
                ));
    }

    /**
     * 서비스 키 확인
     *
     * 접속: http://localhost:8080/debug/check-key
     */
    @GetMapping("/check-key")
    public Map<String, String> checkServiceKey() {
        Map<String, String> result = new HashMap<>();
        result.put("serviceKey_앞10자", serviceKey.substring(0, Math.min(10, serviceKey.length())));
        result.put("serviceKey_길이", String.valueOf(serviceKey.length()));
        result.put("상태", serviceKey.length() > 20 ? "✅ 키가 입력됨" : "❌ 키가 너무 짧음");
        return result;
    }
}