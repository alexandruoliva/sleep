package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.api.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import com.noom.interview.fullstack.sleep.models.SleepLog;
import com.noom.interview.fullstack.sleep.models.User;
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository;
import com.noom.interview.fullstack.sleep.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SleepLogServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SleepLogRepository sleepLogRepository;

    private SleepLogService sleepLogService;

    private static final long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        sleepLogService = new SleepLogService(userRepository, sleepLogRepository);
    }

    @Test
    void createOrUpdateSleepLog_throwsWhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        CreateSleepLogRequest request = validRequest();

        assertThatThrownBy(() -> sleepLogService.createOrUpdateSleepLog(USER_ID, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("" + USER_ID);
    }

    @Test
    void createOrUpdateSleepLog_savesNewAndReturnsResponse() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, Instant.now())));
        when(sleepLogRepository.findByUserIdAndSleepDate(eq(USER_ID), eq(LocalDate.of(2025, 2, 22)))).thenReturn(Optional.empty());
        SleepLog saved = new SleepLog();
        saved.setId(10L);
        saved.setUserId(USER_ID);
        saved.setSleepDate(LocalDate.of(2025, 2, 22));
        saved.setWentToBedAt(LocalTime.of(23, 0));
        saved.setGotUpAt(LocalTime.of(7, 30));
        saved.setTotalTimeInBedMinutes(510);
        saved.setMorningFeeling(MorningFeeling.GOOD);
        saved.setCreatedAt(Instant.now());
        when(sleepLogRepository.save(any(SleepLog.class))).thenReturn(saved);

        CreateSleepLogRequest request = validRequest();
        SleepLogResponse response = sleepLogService.createOrUpdateSleepLog(USER_ID, request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getSleepDate()).isEqualTo(LocalDate.of(2025, 2, 22));
        assertThat(response.getTotalTimeInBedMinutes()).isEqualTo(510);
        assertThat(response.getMorningFeeling()).isEqualTo(MorningFeeling.GOOD);
    }

    @Test
    void createOrUpdateSleepLog_updatesExistingAndReturnsResponse() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, Instant.now())));
        SleepLog existing = new SleepLog();
        existing.setId(5L);
        existing.setUserId(USER_ID);
        existing.setSleepDate(LocalDate.of(2025, 2, 22));
        existing.setMorningFeeling(MorningFeeling.BAD);
        when(sleepLogRepository.findByUserIdAndSleepDate(eq(USER_ID), eq(LocalDate.of(2025, 2, 22)))).thenReturn(Optional.of(existing));
        existing.setMorningFeeling(MorningFeeling.GOOD);
        when(sleepLogRepository.save(any(SleepLog.class))).thenReturn(existing);

        CreateSleepLogRequest request = validRequest();
        request.setMorningFeeling(MorningFeeling.GOOD);
        SleepLogResponse response = sleepLogService.createOrUpdateSleepLog(USER_ID, request);

        assertThat(response.getMorningFeeling()).isEqualTo(MorningFeeling.GOOD);
    }

    @Test
    void createOrUpdateSleepLog_throwsWhenTotalTimeOutOfRange() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, Instant.now())));
        CreateSleepLogRequest request = validRequest();
        request.setTotalTimeInBedMinutes(0);

        assertThatThrownBy(() -> sleepLogService.createOrUpdateSleepLog(USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalTimeInBedMinutes");
    }

    @Test
    void getLastNightSleep_returnsEmptyWhenNoLog() {
        when(sleepLogRepository.findTopByUserIdOrderBySleepDateDesc(USER_ID)).thenReturn(Optional.empty());

        Optional<SleepLogResponse> result = sleepLogService.getLastNightSleep(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getLastNightSleep_returnsResponseWhenLogExists() {
        SleepLog log = new SleepLog();
        log.setId(5L);
        log.setUserId(USER_ID);
        log.setSleepDate(LocalDate.of(2025, 2, 22));
        log.setMorningFeeling(MorningFeeling.OK);
        when(sleepLogRepository.findTopByUserIdOrderBySleepDateDesc(USER_ID)).thenReturn(Optional.of(log));

        Optional<SleepLogResponse> result = sleepLogService.getLastNightSleep(USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
        assertThat(result.get().getMorningFeeling()).isEqualTo(MorningFeeling.OK);
    }

    private static CreateSleepLogRequest validRequest() {
        CreateSleepLogRequest r = new CreateSleepLogRequest();
        r.setSleepDate(LocalDate.of(2025, 2, 22));
        r.setWentToBedAt(LocalTime.of(23, 0));
        r.setGotUpAt(LocalTime.of(7, 30));
        r.setTotalTimeInBedMinutes(510);
        r.setMorningFeeling(MorningFeeling.GOOD);
        return r;
    }
}
