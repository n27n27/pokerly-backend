package com.rolling.pokerly.core.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;

    public static <T> ApiResponse<T> ok(T data) {
        var r = new ApiResponse<T>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String message) {
        var r = new ApiResponse<T>();
        r.success = false;
        r.error = message;
        return r;
    }

}
