package com.rolling.pokerly.user.exception;

import org.springframework.http.HttpStatus;

import com.rolling.pokerly.core.exception.ApiException;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
    }
}
