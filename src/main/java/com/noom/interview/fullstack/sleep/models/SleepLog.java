package com.noom.interview.fullstack.sleep.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JPA entity for a single night's sleep log.
 * sleepDate is the calendar date of the sleep (e.g. today for "last night").
 * morningFeeling is how the user felt in the morning (BAD, OK, GOOD).
 */
@Entity
@Table(name = "sleep_logs", uniqueConstraints = {
        @UniqueConstraint(name = "uq_sleep_log_user_date", columnNames = {"user_id", "sleep_date"})
})
@Getter
@Setter
@NoArgsConstructor
public class SleepLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sleep_date", nullable = false)
    private LocalDate sleepDate;

    @Column(name = "went_to_bed_at", nullable = false)
    private LocalTime wentToBedAt;

    @Column(name = "got_up_at", nullable = false)
    private LocalTime gotUpAt;

    @Getter(AccessLevel.NONE)
    @Column(name = "total_time_in_bed_minutes", nullable = false)
    private Integer totalTimeInBedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "morning_feeling", nullable = false, length = 10)
    private MorningFeeling morningFeeling;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /** Null-safe for uninitialized entity. */
    public int getTotalTimeInBedMinutes() {
        return totalTimeInBedMinutes == null ? 0 : totalTimeInBedMinutes;
    }
}
