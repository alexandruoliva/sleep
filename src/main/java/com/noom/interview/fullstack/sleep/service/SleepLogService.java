package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.api.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.models.SleepLog;
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository;
import com.noom.interview.fullstack.sleep.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
