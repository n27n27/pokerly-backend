package com.rolling.pokerly.core.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.response.ApiResponse;

@RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<String>> handle(ApiException e) {
        return ResponseEntity.badRequest().body(ApiResponse.ok(e.getMessage()));
    }
}
