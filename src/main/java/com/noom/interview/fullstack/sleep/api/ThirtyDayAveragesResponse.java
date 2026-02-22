package com.noom.interview.fullstack.sleep.api;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * Response for the last 30-day sleep averages.
 * Includes the date range, average time in bed, average bed/rise times, and morning feeling frequencies.
 */
@Getter
@Setter
public class ThirtyDayAveragesResponse {

    /** Start of the range (inclusive). */
    private LocalDate rangeStart;
    /** End of the range (inclusive). */
    private LocalDate rangeEnd;
    /** Average total time in bed in minutes. */
    private double averageTotalTimeInBedMinutes;
    /** Average time the user went to bed (clock time). */
    private LocalTime averageWentToBedAt;
    /** Average time the user got out of bed (clock time). */
    private LocalTime averageGotUpAt;
    /** Count per morning feeling: key = BAD, OK, or GOOD. */
    private Map<String, Long> morningFeelingFrequencies;
}
