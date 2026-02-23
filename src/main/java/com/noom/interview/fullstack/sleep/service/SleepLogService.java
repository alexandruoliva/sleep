package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.api.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.api.ThirtyDayAveragesResponse;
import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import com.noom.interview.fullstack.sleep.models.SleepLog;
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository;
import com.noom.interview.fullstack.sleep.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business logic for sleep logs: ensure user exists, validate, create/update, fetch last night.
 */
@Service
public class SleepLogService {

    private final UserRepository userRepository;
    private final SleepLogRepository sleepLogRepository;

    public SleepLogService(UserRepository userRepository, SleepLogRepository sleepLogRepository) {
        this.userRepository = userRepository;
        this.sleepLogRepository = sleepLogRepository;
    }

    /**
     * Creates or updates the sleep log for the given user and date.
     *
     * @return the saved sleep log as response
     */
    @Transactional
    public SleepLogResponse createOrUpdateSleepLog(long userId, CreateSleepLogRequest request) {
        ensureUserExists(userId);
        validateTotalTime(request);

        SleepLog saved = sleepLogRepository.findByUserIdAndSleepDate(userId, request.getSleepDate())
                .map(existing -> {
                    existing.setWentToBedAt(request.getWentToBedAt());
                    existing.setGotUpAt(request.getGotUpAt());
                    existing.setTotalTimeInBedMinutes(request.getTotalTimeInBedMinutes());
                    existing.setMorningFeeling(request.getMorningFeeling());
                    return sleepLogRepository.save(existing);
                })
                .orElseGet(() -> {
                    SleepLog log = new SleepLog();
                    log.setUserId(userId);
                    log.setSleepDate(request.getSleepDate());
                    log.setWentToBedAt(request.getWentToBedAt());
                    log.setGotUpAt(request.getGotUpAt());
                    log.setTotalTimeInBedMinutes(request.getTotalTimeInBedMinutes());
                    log.setMorningFeeling(request.getMorningFeeling());
                    return sleepLogRepository.save(log);
                });

        return SleepLogResponse.from(saved);
    }

    /**
     * Returns the most recent sleep log for the user ("last night's sleep"), if any.
     */
    public Optional<SleepLogResponse> getLastNightSleep(long userId) {
        return sleepLogRepository.findTopByUserIdOrderBySleepDateDesc(userId)
                .map(SleepLogResponse::from);
    }

    /**
     * Returns the last 30-day averages for the user: date range, average time in bed,
     * average went-to-bed and got-up times, and morning feeling frequencies.
     */
    public ThirtyDayAveragesResponse getLast30DayAverages(long userId) {
        ensureUserExists(userId);
        LocalDate rangeEnd = LocalDate.now();
        LocalDate rangeStart = rangeEnd.minusDays(29);
        List<SleepLog> logs = sleepLogRepository.findByUserIdAndSleepDateBetweenOrderBySleepDateAsc(userId, rangeStart, rangeEnd);

        ThirtyDayAveragesResponse response = new ThirtyDayAveragesResponse();
        response.setRangeStart(rangeStart);
        response.setRangeEnd(rangeEnd);

        if (logs.isEmpty()) {
            response.setAverageTotalTimeInBedMinutes(0);
            response.setAverageWentToBedAt(null);
            response.setAverageGotUpAt(null);
            response.setMorningFeelingFrequencies(frequenciesForNone());
            return response;
        }

        double avgMinutes = logs.stream().mapToInt(SleepLog::getTotalTimeInBedMinutes).average().orElse(0);
        response.setAverageTotalTimeInBedMinutes(Math.round(avgMinutes * 10) / 10.0);

        List<LocalTime> wentToBedTimes = logs.stream().map(SleepLog::getWentToBedAt).collect(Collectors.toList());
        List<LocalTime> gotUpTimes = logs.stream().map(SleepLog::getGotUpAt).collect(Collectors.toList());
        response.setAverageWentToBedAt(averageLocalTime(wentToBedTimes));
        response.setAverageGotUpAt(averageLocalTime(gotUpTimes));

        Map<String, Long> fromLogs = logs.stream()
                .collect(Collectors.groupingBy(log -> log.getMorningFeeling().name(), Collectors.counting()));
        Map<String, Long> frequencies = Arrays.stream(MorningFeeling.values())
                .collect(Collectors.toMap(MorningFeeling::name, f -> fromLogs.getOrDefault(f.name(), 0L)));
        response.setMorningFeelingFrequencies(frequencies);

        return response;
    }

    private static Map<String, Long> frequenciesForNone() {
        return Arrays.stream(MorningFeeling.values())
                .collect(Collectors.toMap(MorningFeeling::name, f -> 0L));
    }

    /** Averages non-null LocalTime values (as clock times, minutes since midnight). Returns null if no valid times. */
    private static LocalTime averageLocalTime(List<LocalTime> times) {
        if (times == null || times.isEmpty()) return null;
        List<LocalTime> nonNull = times.stream().filter(t -> t != null).collect(Collectors.toList());
        if (nonNull.isEmpty()) return null;
        int avgMinutes = nonNull.stream()
                .mapToInt(t -> t.getHour() * 60 + t.getMinute() + t.getSecond() / 60)
                .sum() / nonNull.size();
        return LocalTime.of((avgMinutes / 60) % 24, avgMinutes % 60);
    }

    private void ensureUserExists(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException("User not found: " + userId);
        }
    }

    private void validateTotalTime(CreateSleepLogRequest request) {
        int minutes = request.getTotalTimeInBedMinutes();
        if (minutes < 1 || minutes > 24 * 60) {
            throw new IllegalArgumentException("totalTimeInBedMinutes must be between 1 and 1440");
        }
    }
}
