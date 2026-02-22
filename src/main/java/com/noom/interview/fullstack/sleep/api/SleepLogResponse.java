package com.noom.interview.fullstack.sleep.api;

import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import com.noom.interview.fullstack.sleep.models.SleepLog;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response body for a single sleep log.
 */
@Getter
@Setter
public final class SleepLogResponse {

    private long id;
    private long userId;
    private LocalDate sleepDate;
    private LocalTime wentToBedAt;
    private LocalTime gotUpAt;
    private int totalTimeInBedMinutes;
    private MorningFeeling morningFeeling;
    private Instant createdAt;

    public static SleepLogResponse from(SleepLog log) {
        SleepLogResponse r = new SleepLogResponse();
        r.setId(log.getId() != null ? log.getId() : 0L);
        r.setUserId(log.getUserId() != null ? log.getUserId() : 0L);
        r.setSleepDate(log.getSleepDate());
        r.setWentToBedAt(log.getWentToBedAt());
        r.setGotUpAt(log.getGotUpAt());
        r.setTotalTimeInBedMinutes(log.getTotalTimeInBedMinutes());
        r.setMorningFeeling(log.getMorningFeeling());
        r.setCreatedAt(log.getCreatedAt());
        return r;
    }
}
