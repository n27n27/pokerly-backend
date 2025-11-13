package com.rolling.pokerly.gamesession.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.dto.GameSessionRequest;
import com.rolling.pokerly.gamesession.dto.GameSessionResponse;
import com.rolling.pokerly.gamesession.dto.MonthSummaryResponse;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final GameSessionRepository repo;

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static int nz(Integer v, int defaultValue) {
        return v == null ? defaultValue : v;
    }

    private static void computeDerived(GameSession e) {
        BigDecimal totalBuy = nz(e.getBuyIn())
        .multiply(BigDecimal.valueOf(Optional.ofNullable(e.getEntries()).orElse(1)));

        BigDecimal discount = nz(e.getDiscount());
        BigDecimal cashCost = totalBuy.subtract(discount);
        BigDecimal totalCost = totalBuy.add(nz(e.getPointUsed())).subtract(discount);

        e.setProfitCashRealized(nz(e.getCashOut()).subtract(cashCost));
        e.setProfitIncludingPoints(nz(e.getCashOut()).subtract(totalCost));
    }

    private static GameSessionResponse toDto(GameSession e) {
        return GameSessionResponse.builder()
                .id(e.getId())
                .venueId(e.getVenueId())
                .playDate(e.getPlayDate())
                .title(e.getTitle())
                .gameType(e.getGameType())
                .buyIn(e.getBuyIn())
                .entries(e.getEntries())
                .cashOut(e.getCashOut())
                .pointUsed(e.getPointUsed())
                .pointRemainAfter(e.getPointRemainAfter())
                .discount(e.getDiscount())
                .notes(e.getNotes())
                .profitCashRealized(e.getProfitCashRealized())
                .profitIncludingPoints(e.getProfitIncludingPoints())
                .build();
    }

    public GameSessionResponse create(Long userId, GameSessionRequest req) {
        GameSession e = GameSession.builder()
                    .userId(userId)
                    .venueId(req.getVenueId())
                    .playDate(Optional.ofNullable(req.getPlayDate()).orElse(LocalDate.now()))
                    .title(req.getTitle())
                    .gameType(req.getGameType())
                    .buyIn(nz(req.getBuyIn()))
                    .entries(nz(req.getEntries(), 1))
                    .cashOut(nz(req.getCashOut()))
                    .pointUsed(nz(req.getPointUsed()))
                    .pointRemainAfter(nz(req.getPointRemainAfter()))
                    .discount(nz(req.getDiscount()))
                    .notes(req.getNotes())
                    .build();


        computeDerived(e);
        repo.save(e);
        return toDto(e);
    }

    public GameSessionResponse update(Long userId, Long id, GameSessionRequest req) {
        GameSession e = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 게임이 없습니다."));
        if (!e.getUserId().equals(userId)) throw new SecurityException("Forbidden");

        e.setVenueId(req.getVenueId());
        e.setPlayDate(req.getPlayDate());
        e.setTitle(req.getTitle());
        e.setGameType(req.getGameType());
        e.setBuyIn(nz(req.getBuyIn()));
        e.setEntries(req.getEntries());
        e.setCashOut(nz(req.getCashOut()));
        e.setPointUsed(nz(req.getPointUsed()));
        e.setPointRemainAfter(nz(req.getPointRemainAfter()));
        e.setDiscount(nz(req.getDiscount()));
        e.setNotes(req.getNotes());

        computeDerived(e);
        repo.save(e);
        return toDto(e);
    }

    public void delete(Long userId, Long id) {
        GameSession e = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 게임이 없습니다."));

        if (!e.getUserId().equals(userId)) throw new SecurityException("Forbidden");
        repo.delete(e);
    }

    public GameSessionResponse get(Long userId, Long id) {
        GameSession e = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 게임이 없습니다."));
        if (!e.getUserId().equals(userId)) throw new SecurityException("Forbidden");
        return toDto(e);
    }

    public List<GameSessionResponse> listByMonth(Long userId, YearMonth ym, Long venueId) {
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        return repo.findByUserIdAndPlayDateBetween(userId, from, to).stream()
                .filter(e -> venueId == null || e.getVenueId().equals(venueId))
                .sorted((a, b) -> b.getPlayDate().compareTo(a.getPlayDate()))  // 최신순
                .map(GameSessionService::toDto)
                .collect(Collectors.toList());
    }

    public MonthSummaryResponse summaryByMonth(Long userId, YearMonth ym, Long venueId) {
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<GameSession> list = repo.findByUserIdAndPlayDateBetween(userId, from, to).stream()
                .filter(e -> venueId == null || e.getVenueId().equals(venueId))
                .collect(Collectors.toList());

        BigDecimal totalBuy = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCashOut = BigDecimal.ZERO;
        BigDecimal totalPointUsed = BigDecimal.ZERO;
        BigDecimal profitCash = BigDecimal.ZERO;
        BigDecimal profitIncl = BigDecimal.ZERO;

        for (GameSession e : list) {
            BigDecimal buy = nz(e.getBuyIn()).multiply(BigDecimal.valueOf(Optional.ofNullable(e.getEntries()).orElse(0)));

            totalBuy = totalBuy.add(buy);
            totalDiscount = totalDiscount.add(nz(e.getDiscount()));
            totalCashOut = totalCashOut.add(nz(e.getCashOut()));
            totalPointUsed = totalPointUsed.add(nz(e.getPointUsed()));
            profitCash = profitCash.add(nz(e.getProfitCashRealized()));
            profitIncl = profitIncl.add(nz(e.getProfitIncludingPoints()));
        }

        return MonthSummaryResponse.builder()
                .year(ym.getYear())
                .month(ym.getMonthValue())
                .count(list.size())
                .totalBuyIn(totalBuy.subtract(totalDiscount))
                .totalDiscount(totalDiscount)
                .totalCashOut(totalCashOut)
                .totalPointUsed(totalPointUsed)
                .profitCashRealized(profitCash)
                .profitIncludingPoints(profitIncl)
                .build();
    }
}
