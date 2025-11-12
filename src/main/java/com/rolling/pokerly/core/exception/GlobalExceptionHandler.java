package com.rolling.pokerly.core.exception;

import com.rolling.pokerly.core.response.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiErrorResponse body(HttpServletRequest req, HttpStatus status,
                                  String code, String message, Map<String, Object> errors) {
        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(req.getRequestURI())
                .errors(errors)
                .build();
    }

    // ===== 1) 커스텀 비즈니스 예외 =====
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
        log.warn("[API] {} - {}", ex.getCode(), ex.getMessage());
        var res = body(req, ex.getStatus(), ex.getCode(), ex.getMessage(), null);
        return ResponseEntity.status(ex.getStatus()).body(res);
    }

    // ===== 2) 인증/인가 =====
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> badCredentials(BadCredentialsException ex, HttpServletRequest req) {
        var res = body(req, HttpStatus.UNAUTHORIZED, "AUTH_BAD_CREDENTIALS",
                "닉네임 또는 비밀번호가 올바르지 않습니다.", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> accessDenied(AccessDeniedException ex, HttpServletRequest req) {
        var res = body(req, HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "접근 권한이 없습니다.", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> jwt(JwtException ex, HttpServletRequest req) {
        var res = body(req, HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN",
                "유효하지 않거나 만료된 토큰입니다.", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    // ===== 3) 검증 에러 =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> methodArg(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var details = new HashMap<String, Object>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
            details.put(fe.getField(), fe.getDefaultMessage())
        );
        var res = body(req, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "요청 값이 올바르지 않습니다.", details);
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> constraint(ConstraintViolationException ex, HttpServletRequest req) {
        var details = new HashMap<String, Object>();
        ex.getConstraintViolations().forEach(v -> details.put(v.getPropertyPath().toString(), v.getMessage()));
        var res = body(req, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "요청 값이 올바르지 않습니다.", details);
        return ResponseEntity.badRequest().body(res);
    }

    // ===== 4) 데이터 무결성/DB =====
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("[DB] {}", ex.getMostSpecificCause().getMessage());
        var res = body(req, HttpStatus.CONFLICT, "DATA_INTEGRITY",
                "데이터 무결성 제약에 위배되었습니다.", null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    // ===== 5) 그 외 모든 예외 =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unknown(Exception ex, HttpServletRequest req) {
        log.error("[UNHANDLED] {}", ex.getMessage(), ex);
        var res = body(req, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                         HttpServletRequest req) {
        var res = body(req, HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "요청 메서드가 지원되지 않습니다. 지원: ", null);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(res);
    }
}
