package com.noom.interview.fullstack.sleep.api;

import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request body for creating or updating a sleep log (last night).
 */
@Getter
@Setter
public final class CreateSleepLogRequest {

    @NotNull(message = "sleepDate is required")
    private LocalDate sleepDate;

    @NotNull(message = "wentToBedAt is required")
    private LocalTime wentToBedAt;

    @NotNull(message = "gotUpAt is required")
    private LocalTime gotUpAt;

    @NotNull
    @Min(1)
    @Max(24 * 60)
    private Integer totalTimeInBedMinutes;

    @NotNull(message = "morningFeeling is required")
    private MorningFeeling morningFeeling;
}
