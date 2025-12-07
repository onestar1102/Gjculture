package com.example.gjculture.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * 외부 API/비즈니스 오류를 래핑하는 런타임 예외.
 * 컨트롤러(@RestController)에서 잡아서 적절한 상태코드와 메시지로 내려줄 때 사용하세요.
 */
public class ApiException extends RuntimeException {

    private final HttpStatusCode status;

    public ApiException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public ApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
