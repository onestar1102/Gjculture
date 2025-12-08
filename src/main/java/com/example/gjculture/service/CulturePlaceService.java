package com.example.gjculture.service;

import com.example.gjculture.dto.CulturePlaceDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 광주교통공사 "역 인근 문화공간" API 서비스
 * - 공공데이터포털의 광주교통공사 Open API 사용
 * - 역 이름으로 주변 문화시설 정보 조회
 *
 * API 정보:
 * - 엔드포인트: /GET_OAMS_CLTIPLCE_01
 * - 설명: 광주광역시 역 인근 문화공간 정보
 */
@Service
public class CulturePlaceService {

    private static final Logger log = LoggerFactory.getLogger(CulturePlaceService.class);

    // ✅ 실제 엔드포인트
    private static final String ENDPOINT = "/GET_OAMS_CLTIPLCE_01";
    private static final String BASE_HOST = "apis.data.go.kr";
    private static final String BASE_PATH = "/6290000/gjCulturePlaceService";

    private final WebClient webClient;
    private final String serviceKey;
    private final ObjectMapper objectMapper;

    public CulturePlaceService(WebClient webClient,
                               @Value("${app.grtc.service-key}") String serviceKey) {
        this.webClient = webClient;
        this.serviceKey = serviceKey;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 역 주변 문화공간 조회
     * @param stationName 역명 (예: "광주역", "금남로4가")
     * @return 문화공간 목록
     */
    public Mono<List<CulturePlaceDto>> getPlacesNearStation(String stationName) {
        log.info("🔍 광주교통공사 API 호출: 역명={}", stationName);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host(BASE_HOST)
                        .path(BASE_PATH + ENDPOINT)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 100)
                        .queryParam("type", "json")  // 또는 _type
                        // ⚠️ 역명 파라미터 - Swagger에서 정확한 이름 확인 필요
                        // 가능한 이름: stationNm, stationName, 역명, STATION_NM 등
                        .queryParam("stationNm", stationName)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> {
                    log.info("✅ API 응답 받음 (길이: {} bytes)", response.length());
                    log.debug("📄 응답 내용: {}", response.length() > 500 ?
                            response.substring(0, 500) + "..." : response);
                })
                .map(this::parseResponse)
                .doOnError(e -> log.error("❌ API 호출 실패: {}", e.getMessage()))
                .onErrorReturn(List.of()); // 에러 시 빈 리스트 반환
    }

    /**
     * JSON 응답을 CulturePlaceDto 리스트로 변환
     *
     * 공공데이터 표준 응답 구조:
     * {
     *   "response": {
     *     "header": {
     *       "resultCode": "00",
     *       "resultMsg": "NORMAL_SERVICE"
     *     },
     *     "body": {
     *       "items": {
     *         "item": [
     *           {
     *             "placeName": "국립아시아문화전당",
     *             "distance": "500.5",
     *             "latitude": "35.1595",
     *             "longitude": "126.9108",
     *             "category": "박물관",
     *             "address": "광주 동구 문화전당로 38"
     *           }
     *         ]
     *       },
     *       "totalCount": 10
     *     }
     *   }
     * }
     */
    private List<CulturePlaceDto> parseResponse(String jsonStr) {
        List<CulturePlaceDto> result = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonStr);

            // 1. 헤더 확인 (에러 체크)
            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText("");
            String resultMsg = header.path("resultMsg").asText("");

            log.info("📊 API 응답 코드: {} - {}", resultCode, resultMsg);

            // resultCode가 "00" 또는 "0000"이면 정상
            if (!resultCode.equals("00") && !resultCode.equals("0000")) {
                log.warn("⚠️ API 에러 응답: {} - {}", resultCode, resultMsg);
                return result;
            }

            // 2. 데이터 파싱
            JsonNode body = root.path("response").path("body");
            JsonNode items = body.path("items").path("item");

            // item이 배열인 경우
            if (items.isArray()) {
                log.info("📦 아이템 개수: {}", items.size());
                for (JsonNode item : items) {
                    CulturePlaceDto dto = parseItem(item);
                    if (dto.getPlaceName() != null && !dto.getPlaceName().isEmpty()) {
                        result.add(dto);
                    }
                }
            }
            // item이 단일 객체인 경우
            else if (!items.isMissingNode()) {
                log.info("📦 아이템 개수: 1");
                CulturePlaceDto dto = parseItem(items);
                if (dto.getPlaceName() != null && !dto.getPlaceName().isEmpty()) {
                    result.add(dto);
                }
            }

            // totalCount 로그
            int totalCount = body.path("totalCount").asInt(0);
            log.info("✅ 파싱 완료: {}/{} 건", result.size(), totalCount);

        } catch (Exception e) {
            log.error("❌ JSON 파싱 실패: {}", e.getMessage());
            log.error("📄 원본 JSON: {}", jsonStr.length() > 1000 ?
                    jsonStr.substring(0, 1000) + "..." : jsonStr);
        }

        return result;
    }

    /**
     * JSON 항목을 DTO로 변환
     *
     * ⚠️ 실제 필드명은 Swagger 또는 API 응답을 확인하여 수정 필요
     * 가능한 필드명 패턴:
     * - 한글: 문화공간명, 거리, 위도, 경도, 분류, 소재지
     * - 영문: placeName, distance, latitude, longitude, category, address
     * - 대문자: PLACE_NAME, DISTANCE, LAT, LNG, CATEGORY, ADDR
     */
    private CulturePlaceDto parseItem(JsonNode item) {
        CulturePlaceDto dto = new CulturePlaceDto();

        // 장소명 (여러 가능한 필드명 시도)
        dto.setPlaceName(getStringValue(item,
                "문화공간명", "placeName", "PLACE_NAME", "place_name", "name", "NAME"
        ));

        // 거리 (미터)
        dto.setDistance(getDoubleValue(item,
                "거리", "distance", "DISTANCE", "dist", "DIST"
        ));

        // 위도
        dto.setLatitude(getDoubleValue(item,
                "위도", "latitude", "LATITUDE", "lat", "LAT", "y", "Y"
        ));

        // 경도
        dto.setLongitude(getDoubleValue(item,
                "경도", "longitude", "LONGITUDE", "lon", "LON", "lng", "LNG", "x", "X"
        ));

        // 카테고리
        dto.setCategory(getStringValue(item,
                "분류", "카테고리", "category", "CATEGORY", "type", "TYPE"
        ));

        // 주소
        dto.setAddress(getStringValue(item,
                "소재지", "주소", "address", "ADDRESS", "addr", "ADDR"
        ));

        return dto;
    }

    /**
     * JSON에서 문자열 값 가져오기 (여러 필드명 시도)
     */
    private String getStringValue(JsonNode item, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = item.path(fieldName);
            if (!node.isMissingNode() && !node.isNull()) {
                String value = node.asText("").trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return "";
    }

    /**
     * JSON에서 숫자 값 가져오기 (여러 필드명 시도)
     */
    private double getDoubleValue(JsonNode item, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = item.path(fieldName);
            if (!node.isMissingNode() && !node.isNull()) {
                if (node.isNumber()) {
                    return node.asDouble(0.0);
                } else if (node.isTextual()) {
                    try {
                        return Double.parseDouble(node.asText());
                    } catch (NumberFormatException e) {
                        // 계속 다음 필드 시도
                    }
                }
            }
        }
        return 0.0;
    }
}