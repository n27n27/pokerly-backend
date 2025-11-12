package com.rolling.pokerly.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.core.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // 1) 인증 필요 없는 공개 엔드포인트
    @GetMapping("/public")
    public ApiResponse<String> publicPing() {
        return ApiResponse.ok("public ok");
    }

    // 2) 인증 필요: 토큰 있으면 200, 없으면 401 (JwtAuthenticationEntryPoint)
    @GetMapping("/me")
    public ApiResponse<String> me(Authentication authentication) {
        return ApiResponse.ok("hello " + authentication.getName());
    }

    // 3) 권한 필요: ADMIN 아니면 403 (JwtAccessDeniedHandler)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    public ApiResponse<String> adminOnly() {
        return ApiResponse.ok("admin ok");
    }

    // 4) 커스텀 비즈니스 예외 → GlobalExceptionHandler(ApiException)로 400 변환
    @GetMapping("/biz-error")
    public ApiResponse<Void> bizError() {
        throw new ApiException(HttpStatus.BAD_REQUEST, "DEMO_BUSINESS_ERROR", "비즈니스 규칙 위반(데모)");
    }

    // 5) 런타임 예외 → GlobalExceptionHandler(Exception)로 500 변환
    @GetMapping("/runtime-error")
    public ApiResponse<Void> runtimeError() {
        throw new RuntimeException("demo NPE");
    }

    // 6) @Valid 검증 실패 → 400 VALIDATION_ERROR
    @PostMapping("/validate")
    public ApiResponse<String> validate(@RequestBody @Valid SampleDto dto) {
        return ApiResponse.ok("ok:" + dto.getValue());
    }

    @Getter @Setter
    public static class SampleDto {
        @NotBlank(message = "value는 필수입니다.")
        private String value;
    }
}
