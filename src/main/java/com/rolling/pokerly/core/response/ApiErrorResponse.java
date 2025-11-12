package com.rolling.pokerly.core.response;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private Instant timestamp;            // UTC
    private int status;                   // 401, 403, 409, ...
    private String code;                  // 도메인/보안 에러 코드 (예: AUTH_BAD_CREDENTIALS)
    private String message;               // 사용자 메시지
    private String path;                  // 요청 경로
    private Map<String, Object> errors;   // 필드 검증 상세 (nullable)
}
