package com.rolling.pokerly.point.dto;

import java.time.LocalDateTime;

import com.rolling.pokerly.point.domain.PointTransaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionResponse {

    private Long id;
    private Long venueId;
    private Long gameSessionId;
    private Long changeAmount;
    private Long balanceAfter;
    private String type;
    private String description;
    private LocalDateTime createdAt;

    public static PointTransactionResponse from(PointTransaction t) {
        return PointTransactionResponse.builder()
                .id(t.getId())
                .venueId(t.getVenueId())
                .gameSessionId(t.getGameSessionId())
                .changeAmount(t.getChangeAmount())
                .balanceAfter(t.getBalanceAfter())
                .type(t.getType())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
