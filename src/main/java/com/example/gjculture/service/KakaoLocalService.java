package com.example.gjculture.service;

import com.example.gjculture.util.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Kakao Local REST API 래퍼
 * - 주소→좌표 변환: /v2/local/search/address.json
 * - 키워드 장소 검색: /v2/local/search/keyword.json
 *
 * 참고: https://developers.kakao.com/docs/latest/ko/local/dev-guide
 */
@Service
public class KakaoLocalService {

    private static final String KAKAO_LOCAL_BASE = "https://dapi.kakao.com";
    private final WebClient webClient;
    private final String restApiKey;

    public KakaoLocalService(WebClient webClient,
                             @Value("${app.kakao.rest-key}") String restApiKey) {
        this.webClient = webClient.mutate()
                .baseUrl(KAKAO_LOCAL_BASE)
                .build();
        this.restApiKey = restApiKey;
    }

    /**
     * 주소 문자열을 좌표로 변환 (지번/도로명 모두 지원)
     * @param address 주소(예: "광주 북구 무등로 123")
     * @return Coord(x:경도, y:위도)
     */
    public Mono<Coord> geocodeAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return Mono.error(new ApiException(HttpStatus.BAD_REQUEST, "주소가 비어 있습니다."));
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/address.json")
                        .queryParam("query", address)
                        .build())
                .headers(h -> h.set("Authorization", "KakaoAK " + restApiKey))
                .retrieve()
                // ✅ HttpStatusCode 프레디킷 + Throwable mono 반환
                .onStatus(status -> status.is4xxClientError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new ApiException(resp.statusCode(), "Kakao Local 4xx: " + body))
                )
                .onStatus(status -> status.is5xxServerError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new ApiException(resp.statusCode(), "Kakao Local 5xx: " + body))
                )
                .bodyToMono(AddressSearchResponse.class)
                .timeout(Duration.ofSeconds(5))
                .flatMap(res -> {
                    if (res == null || res.documents == null || res.documents.isEmpty()) {
                        return Mono.error(new ApiException(HttpStatus.NOT_FOUND, "좌표를 찾지 못했습니다."));
                    }
                    AddressDocument doc = res.documents.get(0);
                    return Mono.just(new Coord(parseDouble(doc.x), parseDouble(doc.y)));
                });

    }

    /**
     * 키워드로 장소 검색 (예: "박물관", "미술관")
     * @param query   검색어
     * @param x       중심경도(선택)
     * @param y       중심위도(선택)
     * @param radiusM 반경(미터, 0~20000, 선택)
     * @return Place 리스트
     */
    public Mono<List<Place>> searchKeyword(String query, Double x, Double y, Integer radiusM) {
        if (!StringUtils.hasText(query)) {
            return Mono.error(new ApiException(HttpStatus.BAD_REQUEST, "검색어가 비어 있습니다."));
        }

        return webClient.get()
                .uri(uriBuilder -> {
                    var b = uriBuilder.path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .queryParam("size", 15);
                    if (x != null && y != null) b.queryParam("x", x).queryParam("y", y);
                    if (radiusM != null) b.queryParam("radius", Math.max(0, Math.min(20000, radiusM)));
                    return b.build();
                })
                .headers(h -> h.set("Authorization", "KakaoAK " + restApiKey))
                .retrieve()
                // 🔧 여기 두 군데 수정
                .onStatus(status -> status.is4xxClientError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new ApiException(resp.statusCode(), "Kakao Local 4xx: " + body))
                )
                .onStatus(status -> status.is5xxServerError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new ApiException(resp.statusCode(), "Kakao Local 5xx: " + body))
                )
                .bodyToMono(KeywordSearchResponse.class)
                .timeout(Duration.ofSeconds(5))
                .map(res -> res.documents == null ? List.of() : res.documents.stream()
                        .map(d -> new Place(
                                d.id,
                                d.place_name,
                                safe(d.address_name),
                                safe(d.road_address_name),
                                parseDouble(d.x),
                                parseDouble(d.y),
                                safe(d.phone),
                                safe(d.place_url),
                                safe(d.category_group_name)
                        )).toList());
    }


    private static String safe(String s) { return s == null ? "" : s; }
    private static Double parseDouble(String s) {
        try { return s == null ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }

    /* ====== API 응답 매핑용 내부 타입 ====== */

    // 주소 검색 응답
    public static class AddressSearchResponse {
        public List<AddressDocument> documents;
        public Meta meta;
    }
    public static class AddressDocument {
        public String x; // 경도
        public String y; // 위도
        // 추가 필드가 필요하면 확장 가능
    }
    public static class Meta {
        public int total_count;
    }

    // 키워드 검색 응답
    public static class KeywordSearchResponse {
        public List<KeywordDocument> documents;
        public Meta meta;
    }
    public static class KeywordDocument {
        public String id;
        public String place_name;
        public String address_name;
        public String road_address_name;
        public String x; // 경도
        public String y; // 위도
        public String phone;
        public String place_url;
        public String category_group_name;
    }

    /* ====== 서비스에서 반환할 간단 DTO ====== */

    /** 좌표 DTO (x:경도, y:위도) */
    public record Coord(Double x, Double y) {}

    /** 장소 요약 DTO */
    public record Place(
            String id,
            String name,
            String address,
            String roadAddress,
            Double x,
            Double y,
            String phone,
            String url,
            String category
    ) {}
}
