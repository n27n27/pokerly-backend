package com.rolling.pokerly.point.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointEarnRequest {
    private Long venueId;
    private Long amount;
    private String description;
}
