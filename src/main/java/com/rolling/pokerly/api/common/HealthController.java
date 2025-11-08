package com.rolling.pokerly.api.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/v1/health")
    public String helth() {
        return "OK";
    }
}
