package com.noom.interview.fullstack.sleep.api;

import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response body for a single sleep log.
 */
@Getter
@Setter
public final class SleepLogResponse {

    private UUID id;
    private UUID userId;
    private LocalDate sleepDate;
    private LocalTime wentToBedAt;
    private LocalTime gotUpAt;
    private int totalTimeInBedMinutes;
    private MorningFeeling morningFeeling;
    private Instant createdAt;
}
