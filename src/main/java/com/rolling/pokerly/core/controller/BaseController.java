package com.rolling.pokerly.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.security.jwt.CustomPrincipal;

public abstract class BaseController {

    protected Long getUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ApiException(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "로그인이 필요합니다."
            );
        }
        return ((CustomPrincipal) auth.getPrincipal()).getUserId();
    }
}
