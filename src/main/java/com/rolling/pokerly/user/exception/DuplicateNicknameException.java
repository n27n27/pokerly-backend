package com.rolling.pokerly.user.exception;

import org.springframework.http.HttpStatus;

import com.rolling.pokerly.core.exception.ApiException;

public class DuplicateNicknameException extends ApiException {
    public DuplicateNicknameException() {
        super(HttpStatus.CONFLICT, "USER_DUPLICATE", "이미 사용 중인 닉네임입니다.");
    }
}
