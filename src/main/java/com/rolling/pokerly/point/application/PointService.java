package com.rolling.pokerly.point.application;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.point.domain.PointTransaction;
import com.rolling.pokerly.point.dto.PointAdjustRequest;
import com.rolling.pokerly.point.dto.PointBalanceResponse;
import com.rolling.pokerly.point.dto.PointEarnRequest;
import com.rolling.pokerly.point.dto.PointTransactionResponse;
import com.rolling.pokerly.point.dto.PointUseRequest;
import com.rolling.pokerly.point.repo.PointTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private static final String TYPE_EARN = "EARN";
    private static final String TYPE_USE = "USE";
    private static final String TYPE_ADJUST = "ADJUST";

    private final PointTransactionRepository pointTransactionRepository;

    public PointBalanceResponse getBalance(Long userId, Long venueId) {
        var last = pointTransactionRepository
                .findTopByUserIdAndVenueIdOrderByIdDesc(userId, venueId);

        long balance = last.map(PointTransaction::getBalanceAfter).orElse(0L);

        return PointBalanceResponse.builder()
                .venueId(venueId)
                .balance(balance)
                .build();
    }

    public List<PointTransactionResponse> getTransactions(Long userId, Long venueId, Long limit) {
        var list = pointTransactionRepository
                .findByUserIdAndVenueIdOrderByIdDesc(userId, venueId);

        if (limit != null && list.size() > limit) {
            list = list.subList(0, limit.intValue());
        }

        return list.stream()
                .map(PointTransactionResponse::from)
                .toList();
    }

    @Transactional
    public PointTransactionResponse earn(Long userId, PointEarnRequest req) {
        var venueId = req.getVenueId();
        var rawAmount = Objects.requireNonNullElse(req.getAmount(), 0L);

        if (rawAmount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "적립 포인트는 0보다 커야 합니다.");
        }

        long currentBalance = getCurrentBalance(userId, venueId);
        long newBalance = currentBalance + rawAmount;

        var tx = PointTransaction.builder()
                .userId(userId)
                .venueId(venueId)
                .gameSessionId(null)
                .changeAmount(rawAmount)
                .balanceAfter(newBalance)
                .type(TYPE_EARN)
                .description(req.getDescription())
                .build();

        var saved = pointTransactionRepository.save(tx);
        return PointTransactionResponse.from(saved);
    }

    @Transactional
    public PointTransactionResponse use(Long userId, PointUseRequest req) {
        var venueId = req.getVenueId();
        var rawAmount = Objects.requireNonNullElse(req.getAmount(), 0L);

        if (rawAmount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "사용 포인트는 0보다 커야 합니다.");
        }

        long currentBalance = getCurrentBalance(userId, venueId);
        long change = -rawAmount;
        long newBalance = currentBalance + change;

        if (newBalance < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_POINTS", "포인트가 부족합니다.");
        }

        var tx = PointTransaction.builder()
                .userId(userId)
                .venueId(venueId)
                .gameSessionId(req.getGameSessionId())
                .changeAmount(change)
                .balanceAfter(newBalance)
                .type(TYPE_USE)
                .description(req.getDescription())
                .build();

        var saved = pointTransactionRepository.save(tx);
        return PointTransactionResponse.from(saved);
    }

    @Transactional
    public PointTransactionResponse adjust(Long userId, PointAdjustRequest req) {
        var venueId = req.getVenueId();
        var amount = Objects.requireNonNullElse(req.getAmount(), 0L);

        if (amount == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "조정 포인트는 0이어선 안 됩니다.");
        }

        long currentBalance = getCurrentBalance(userId, venueId);
        long newBalance = currentBalance + amount;

        if (newBalance < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_POINTS", "포인트가 부족합니다.");
        }

        var tx = PointTransaction.builder()
                .userId(userId)
                .venueId(venueId)
                .gameSessionId(null)
                .changeAmount(amount)
                .balanceAfter(newBalance)
                .type(TYPE_ADJUST)
                .description(req.getDescription())
                .build();

        var saved = pointTransactionRepository.save(tx);
        return PointTransactionResponse.from(saved);
    }

    private long getCurrentBalance(Long userId, Long venueId) {
        return pointTransactionRepository
                .findTopByUserIdAndVenueIdOrderByIdDesc(userId, venueId)
                .map(PointTransaction::getBalanceAfter)
                .orElse(0L);
    }

    @Transactional
    public void earnFromSession(Long userId, Long venueId, Long gameSessionId, Long amount) {
        if (amount == null || amount <= 0) {
            return;
        }

        long currentBalance = getCurrentBalance(userId, venueId);
        long newBalance = currentBalance + amount;

        var tx = PointTransaction.builder()
                .userId(userId)
                .venueId(venueId)
                .gameSessionId(gameSessionId)
                .changeAmount(amount)
                .balanceAfter(newBalance)
                .type(TYPE_EARN)
                .description("세션 포인트 적립")
                .build();

        pointTransactionRepository.save(tx);
    }

    @Transactional
    public void adjustSessionEarnedPoint(Long userId, Long venueId, Long gameSessionId, Long diff) {
        if (diff == null || diff == 0L) {
            return;
        }

        long currentBalance = getCurrentBalance(userId, venueId);
        long newBalance = currentBalance + diff;
        if (newBalance < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_POINTS", "포인트가 부족합니다.");
        }

        var tx = PointTransaction.builder()
                .userId(userId)
                .venueId(venueId)
                .gameSessionId(gameSessionId)
                .changeAmount(diff)
                .balanceAfter(newBalance)
                .type(TYPE_ADJUST)
                .description("세션 포인트 수정")
                .build();

        pointTransactionRepository.save(tx);
    }

    @Transactional
    public void rollbackSessionEarnedPoint(Long userId, Long venueId, Long gameSessionId, Long amount) {
        if (amount == null || amount <= 0L) {
            return;
        }

        long currentBalance = getCurrentBalance(userId, venueId);
        long newBalance = currentBalance - amount;
        if (newBalance < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_POINTS", "포인트가 부족합니다.");
        }

        var tx = PointTransaction.builder()
                .userId(userId)
                .venueId(venueId)
                .gameSessionId(gameSessionId)
                .changeAmount(-amount)
                .balanceAfter(newBalance)
                .type(TYPE_ADJUST)
                .description("세션 삭제로 인한 포인트 회수")
                .build();

        pointTransactionRepository.save(tx);
    }

}
