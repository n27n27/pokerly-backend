package com.rolling.pokerly.user.exception;

import org.springframework.http.HttpStatus;

import com.rolling.pokerly.core.exception.ApiException;

public class InvalidRefreshTokenException extends ApiException {
    public InvalidRefreshTokenException() {
        super(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH", "유효하지 않거나 만료된 리프레시 토큰입니다.");
    }
}
