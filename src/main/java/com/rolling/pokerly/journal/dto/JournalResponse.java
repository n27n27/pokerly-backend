package com.rolling.pokerly.journal.dto;

public record JournalResponse(
        Long id,
        String journalDate,
        String title,
        String content,
        Integer moodScore,
        Integer focusScore,
        Integer tiltScore,
        Integer energyScore,
        String tags
) {
}
