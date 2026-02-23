package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.models.SleepLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for sleep logs. One log per user per calendar date.
 */
public interface SleepLogRepository extends JpaRepository<SleepLog, UUID> {

    Optional<SleepLog> findByUserIdAndSleepDate(UUID userId, LocalDate sleepDate);

    Optional<SleepLog> findTopByUserIdOrderBySleepDateDesc(UUID userId);

    List<SleepLog> findByUserIdAndSleepDateBetweenOrderBySleepDateAsc(UUID userId, LocalDate start, LocalDate end);
}
