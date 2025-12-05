package com.rolling.pokerly.journal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JournalRequest(

        @NotBlank
        String journalDate, // YYYY-MM-DD

        @Size(max = 200)
        String title,

        @Size(max = 5000)
        String content,

        @Min(1) @Max(5)
        Integer moodScore,

        @Min(1) @Max(5)
        Integer focusScore,

        @Min(1) @Max(5)
        Integer tiltScore,

        @Min(1) @Max(5)
        Integer energyScore,

        @Size(max = 300)
        String tags
) {
}
