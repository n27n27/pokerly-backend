package com.rolling.pokerly.tools.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.tools.application.ToolUsageService;
import com.rolling.pokerly.tools.dto.ToolUsageRequest;

@RestController
public class ToolUsageController {

    private final ToolUsageService usageService;

    public ToolUsageController(ToolUsageService usageService) {
        this.usageService = usageService;
    }

    @PostMapping("/api/tools/usage")
    public ApiResponse<Void> logUsage(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody ToolUsageRequest req
    ) {
        usageService.logUsage(userId, req);

        return ApiResponse.ok(null);
    }
}
