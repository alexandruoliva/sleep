package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.models.SleepLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * JPA repository for sleep logs. One log per user per calendar date.
 */
public interface SleepLogRepository extends JpaRepository<SleepLog, Long> {

    Optional<SleepLog> findByUserIdAndSleepDate(Long userId, LocalDate sleepDate);

    Optional<SleepLog> findTopByUserIdOrderBySleepDateDesc(Long userId);
}
