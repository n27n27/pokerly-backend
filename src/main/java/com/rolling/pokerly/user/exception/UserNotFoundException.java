package com.rolling.pokerly.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String nickname) {
        super("사용자를 찾을 수 없음: " + nickname);
    }
}
