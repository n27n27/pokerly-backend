package com.rolling.pokerly.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rolling.pokerly.core.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) {
        log.debug("401 entrypoint for {}", req.getRequestURI());

        try {
            var body = ApiErrorResponse.builder()
                    .timestamp(Instant.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .code("AUTH_UNAUTHORIZED")
                    .message("인증이 필요합니다.")
                    .path(req.getRequestURI())
                    .build();
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(res.getWriter(), body);
            res.getWriter().flush();
        } catch (Exception ignored) {}
    }
}
