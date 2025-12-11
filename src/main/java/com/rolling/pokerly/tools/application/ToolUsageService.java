package com.rolling.pokerly.tools.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.tools.domain.ToolUsageEvent;
import com.rolling.pokerly.tools.dto.ToolUsageRequest;
import com.rolling.pokerly.tools.repo.ToolUsageEventRepository;

@Service
public class ToolUsageService {

    private final ToolUsageEventRepository repository;

    public ToolUsageService(ToolUsageEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void logUsage(Long userId, ToolUsageRequest req) {
        ToolUsageEvent event = ToolUsageEvent.builder()
                .userId(userId)
                .toolCode(req.toolCode())
                .action(req.action())
                .usedAt(LocalDateTime.now())
                .build();

        repository.save(event);
    }
}
