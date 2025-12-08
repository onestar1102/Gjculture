package org.example.gjculture;  // ⚠️ 패키지명 통일 (org.example -> com.example)

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 광주 문화공간 검색 애플리케이션 메인 클래스
 * - Spring Boot 애플리케이션의 시작점
 * - 카카오 로컬 API를 사용하여 역 주변 문화공간 검색
 */
@SpringBootApplication
public class GjCultureApplication {
    public static void main(String[] args) {
        SpringApplication.run(GjCultureApplication.class, args);
    }
}