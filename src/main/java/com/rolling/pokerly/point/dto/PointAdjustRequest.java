package com.rolling.pokerly.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointAdjustRequest {
    private Long venueId;
    private Long amount;       // +면 증가, -면 감소
    private String description;
}
