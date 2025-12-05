package com.rolling.pokerly.journal.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.journal.application.JournalService;
import com.rolling.pokerly.journal.dto.JournalCalendarItemResponse;
import com.rolling.pokerly.journal.dto.JournalRequest;
import com.rolling.pokerly.journal.dto.JournalResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    public ApiResponse<JournalResponse> create(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody JournalRequest req
    ) {
        var res = journalService.create(userId, req);
        return ApiResponse.ok(res);
    }

    @GetMapping
    public ApiResponse<JournalResponse> getByDate(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam("date") String date
    ) {
        var res = journalService.getByDate(userId, date);
        return ApiResponse.ok(res);
    }

    @GetMapping("/{id}")
    public ApiResponse<JournalResponse> getById(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("id") Long id
    ) {
        var res = journalService.getById(userId, id);
        return ApiResponse.ok(res);
    }

    @PutMapping("/{id}")
    public ApiResponse<JournalResponse> update(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("id") Long id,
            @Valid @RequestBody JournalRequest req
    ) {
        var res = journalService.update(userId, id, req);
        return ApiResponse.ok(res);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("id") Long id
    ) {
        journalService.delete(userId, id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/monthly")
    public ApiResponse<List<JournalCalendarItemResponse>> getMonthly(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        var list = journalService.getMonthly(userId, year, month);
        return ApiResponse.ok(list);
    }
}
