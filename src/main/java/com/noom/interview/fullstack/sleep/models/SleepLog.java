package com.noom.interview.fullstack.sleep.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A single night's sleep log for a user.
 * <p>
 * sleepDate is the calendar date of the sleep (e.g. today for "last night").
 * morningFeeling is how the user felt in the morning (BAD, OK, GOOD).
 */
public final class SleepLog {

    private final long id;
    private final long userId;
    private final LocalDate sleepDate;
    private final LocalTime wentToBedAt;
    private final LocalTime gotUpAt;
    private final int totalTimeInBedMinutes;
    private final MorningFeeling morningFeeling;
    private final Instant createdAt;

    public SleepLog(
            long id,
            long userId,
            LocalDate sleepDate,
            LocalTime wentToBedAt,
            LocalTime gotUpAt,
            int totalTimeInBedMinutes,
            MorningFeeling morningFeeling,
            Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.sleepDate = sleepDate;
        this.wentToBedAt = wentToBedAt;
        this.gotUpAt = gotUpAt;
        this.totalTimeInBedMinutes = totalTimeInBedMinutes;
        this.morningFeeling = morningFeeling;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public LocalDate getSleepDate() {
        return sleepDate;
    }

    public LocalTime getWentToBedAt() {
        return wentToBedAt;
    }

    public LocalTime getGotUpAt() {
        return gotUpAt;
    }

    public int getTotalTimeInBedMinutes() {
        return totalTimeInBedMinutes;
    }

    public MorningFeeling getMorningFeeling() {
        return morningFeeling;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
