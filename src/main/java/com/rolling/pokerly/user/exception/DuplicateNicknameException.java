package com.rolling.pokerly.user.exception;

import com.rolling.pokerly.core.exception.ApiException;

public class DuplicateNicknameException extends ApiException {
    public DuplicateNicknameException(String nickname) {
        super("이미 사용중인 닉네임: " + nickname);
    }
}
