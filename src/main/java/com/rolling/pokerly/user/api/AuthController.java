package com.rolling.pokerly.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.security.jwt.JwtTokenProvider;
import com.rolling.pokerly.user.application.RefreshTokenService;
import com.rolling.pokerly.user.application.UserService;
import com.rolling.pokerly.user.dto.AuthResponse;
import com.rolling.pokerly.user.dto.LoginRequest;
import com.rolling.pokerly.user.dto.RefreshRequest;
import com.rolling.pokerly.user.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager; // SecurityConfig에서 주입
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {

        var token = new UsernamePasswordAuthenticationToken(
                request.getNickname(),
                request.getPassword()
        );
        authenticationManager.authenticate(token);

        var u = userService.loadUser(request.getNickname());
        var access = tokenProvider.createAccessToken(u.getId(), u.getNickname(), u.getRole());
        var refresh = tokenProvider.createRefreshToken(u.getNickname());
        var refreshExp = tokenProvider.extractExpiry(refresh);

        refreshTokenService.save(u.getNickname(), refresh, refreshExp);

        var auth = AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .user(UserResponse.from(u))
                .build();

        return ApiResponse.ok(auth);
    }

    // 리프레시 토큰 갱신
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        var refreshToken = req.refreshToken();
        log.info("리프레시 요청 토큰={}", refreshToken);

        // 1) 토큰에서 subject(닉네임) 추출
        var nickname = tokenProvider.extractSubject(refreshToken);

        // 2) 저장된 refresh 와 일치 & 미만료 확인
        if (!refreshTokenService.validate(nickname, refreshToken)) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_INVALID_REFRESH",
                    "유효하지 않거나 만료된 리프레시 토큰입니다."
            );
        }

        // 3) 유저 로드
        var u = userService.loadUser(nickname);

        // 4) 새 액세스 / 리프레시 토큰 발급
        var newAccess = tokenProvider.createAccessToken(u.getId(), u.getNickname(), u.getRole());
        var newRefresh = tokenProvider.createRefreshToken(u.getNickname());

        // 5) 리프레시 토큰 저장 (기존 토큰 덮어쓰기)
        var exp = tokenProvider.extractExpiry(newRefresh);
        refreshTokenService.save(u.getNickname(), newRefresh, exp);

        var auth = AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .user(UserResponse.from(u))
                .build();

        return ApiResponse.ok(auth);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication) {
        var nickname = authentication.getName();   // JWT subject = nickname
        refreshTokenService.delete(nickname);      // DB에서 리프레시 토큰 제거
        // 삭제 결과는 굳이 리턴 데이터 필요 없으니 null 로 ok
        return ApiResponse.ok(null);
    }
}
