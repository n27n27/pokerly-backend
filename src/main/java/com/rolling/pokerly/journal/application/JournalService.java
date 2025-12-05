package com.rolling.pokerly.journal.application;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.journal.domain.GameJournal;
import com.rolling.pokerly.journal.dto.JournalCalendarItemResponse;
import com.rolling.pokerly.journal.dto.JournalRequest;
import com.rolling.pokerly.journal.dto.JournalResponse;
import com.rolling.pokerly.journal.repo.GameJournalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JournalService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final GameJournalRepository gameJournalRepository;

    @Transactional
    public JournalResponse create(Long userId, JournalRequest req) {

        LocalDate date = parseDate(req.journalDate());

        gameJournalRepository.findByUserIdAndJournalDate(userId, date)
                .ifPresent(j -> {
                    throw new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "JOURNAL_ALREADY_EXISTS",
                            "해당 날짜의 일지가 이미 존재합니다."
                    );
                });

        var journal = GameJournal.builder()
                .userId(userId)
                .journalDate(date)
                .title(req.title())
                .content(req.content())
                .moodScore(req.moodScore())
                .focusScore(req.focusScore())
                .tiltScore(req.tiltScore())
                .energyScore(req.energyScore())
                .tags(req.tags())
                .build();

        var saved = gameJournalRepository.save(journal);
        return toResponse(saved);
    }

    public JournalResponse getByDate(Long userId, String dateStr) {
        LocalDate date = parseDate(dateStr);

        var journal = gameJournalRepository.findByUserIdAndJournalDate(userId, date)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "JOURNAL_NOT_FOUND",
                        "해당 날짜의 일지를 찾을 수 없습니다."
                ));

        return toResponse(journal);
    }

    public JournalResponse getById(Long userId, Long id) {
        var journal = getOwnedJournal(userId, id);
        return toResponse(journal);
    }

    @Transactional
    public JournalResponse update(Long userId, Long id, JournalRequest req) {
        var journal = getOwnedJournal(userId, id);

        journal.setTitle(req.title());
        journal.setContent(req.content());
        journal.setMoodScore(req.moodScore());
        journal.setFocusScore(req.focusScore());
        journal.setTiltScore(req.tiltScore());
        journal.setEnergyScore(req.energyScore());
        journal.setTags(req.tags());

        return toResponse(journal);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        var journal = getOwnedJournal(userId, id);
        gameJournalRepository.delete(journal);
    }

    public List<JournalCalendarItemResponse> getMonthly(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return gameJournalRepository.findByUserIdAndJournalDateBetween(userId, start, end)
                .stream()
                .map(j -> new JournalCalendarItemResponse(
                        j.getJournalDate().format(DATE_FORMATTER),
                        j.getId(),
                        j.getTitle()
                ))
                .toList();
    }

    // ---------------------

    private GameJournal getOwnedJournal(Long userId, Long id) {
        var journal = gameJournalRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "JOURNAL_NOT_FOUND",
                        "일지를 찾을 수 없습니다."
                ));

        if (!journal.getUserId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "해당 리소스에 접근할 수 없습니다."
            );
        }
        return journal;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DATE",
                    "날짜 형식이 올바르지 않습니다. (예: 2025-12-03)"
            );
        }
    }

    private JournalResponse toResponse(GameJournal j) {
        return new JournalResponse(
                j.getId(),
                j.getJournalDate().format(DATE_FORMATTER),
                j.getTitle(),
                j.getContent(),
                j.getMoodScore(),
                j.getFocusScore(),
                j.getTiltScore(),
                j.getEnergyScore(),
                j.getTags()
        );
    }
}
