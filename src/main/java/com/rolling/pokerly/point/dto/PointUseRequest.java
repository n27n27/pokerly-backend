package com.rolling.pokerly.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointUseRequest {
    private Long venueId;
    private Long gameSessionId; // 선택
    private Long amount;
    private String description;
}
