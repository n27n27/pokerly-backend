package com.rolling.pokerly.tools.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.tools.domain.ToolUsageEvent;

public interface ToolUsageEventRepository extends JpaRepository<ToolUsageEvent, Long> {
}
