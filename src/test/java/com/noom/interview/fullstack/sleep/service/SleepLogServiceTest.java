package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.api.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.api.ThirtyDayAveragesResponse;
import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import com.noom.interview.fullstack.sleep.models.SleepLog;
import com.noom.interview.fullstack.sleep.mapper.SleepLogMapper;
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
import java.util.Arrays;
import java.util.List;
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
    @Mock
    private SleepLogMapper sleepLogMapper;

    private SleepLogService sleepLogService;

    private static final long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        sleepLogService = new SleepLogService(userRepository, sleepLogRepository, sleepLogMapper);
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
        when(sleepLogMapper.toEntity(any(CreateSleepLogRequest.class), eq(USER_ID))).thenReturn(new SleepLog());
        when(sleepLogRepository.save(any(SleepLog.class))).thenReturn(saved);
        SleepLogResponse expectedResponse = new SleepLogResponse();
        expectedResponse.setId(10L);
        expectedResponse.setUserId(USER_ID);
        expectedResponse.setSleepDate(LocalDate.of(2025, 2, 22));
        expectedResponse.setTotalTimeInBedMinutes(510);
        expectedResponse.setMorningFeeling(MorningFeeling.GOOD);
        when(sleepLogMapper.toResponse(any(SleepLog.class))).thenReturn(expectedResponse);

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
        SleepLogResponse expectedResponse = new SleepLogResponse();
        expectedResponse.setMorningFeeling(MorningFeeling.GOOD);
        when(sleepLogMapper.toResponse(any(SleepLog.class))).thenReturn(expectedResponse);

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
        SleepLogResponse expectedResponse = new SleepLogResponse();
        expectedResponse.setId(5L);
        expectedResponse.setMorningFeeling(MorningFeeling.OK);
        when(sleepLogMapper.toResponse(any(SleepLog.class))).thenReturn(expectedResponse);

        Optional<SleepLogResponse> result = sleepLogService.getLastNightSleep(USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
        assertThat(result.get().getMorningFeeling()).isEqualTo(MorningFeeling.OK);
    }

    @Test
    void getLast30DayAverages_throwsWhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sleepLogService.getLast30DayAverages(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getLast30DayAverages_returnsRangeAndZerosWhenNoLogs() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, Instant.now())));
        when(sleepLogRepository.findByUserIdAndSleepDateBetweenOrderBySleepDateAsc(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        ThirtyDayAveragesResponse response = sleepLogService.getLast30DayAverages(USER_ID);

        assertThat(response.getRangeStart()).isNotNull();
        assertThat(response.getRangeEnd()).isNotNull();
        assertThat(response.getRangeEnd()).isEqualTo(response.getRangeStart().plusDays(29));
        assertThat(response.getAverageTotalTimeInBedMinutes()).isEqualTo(0);
        assertThat(response.getAverageWentToBedAt()).isNull();
        assertThat(response.getAverageGotUpAt()).isNull();
        assertThat(response.getMorningFeelingFrequencies()).containsKeys("BAD", "OK", "GOOD");
        assertThat(response.getMorningFeelingFrequencies().values()).containsOnly(0L);
    }

    @Test
    void getLast30DayAverages_returnsAveragesAndFrequenciesWhenLogsExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, Instant.now())));
        SleepLog log1 = new SleepLog();
        log1.setTotalTimeInBedMinutes(480);
        log1.setWentToBedAt(LocalTime.of(23, 0));
        log1.setGotUpAt(LocalTime.of(7, 0));
        log1.setMorningFeeling(MorningFeeling.OK);
        SleepLog log2 = new SleepLog();
        log2.setTotalTimeInBedMinutes(420);
        log2.setWentToBedAt(LocalTime.of(22, 30));
        log2.setGotUpAt(LocalTime.of(6, 30));
        log2.setMorningFeeling(MorningFeeling.GOOD);
        List<SleepLog> logs = Arrays.asList(log1, log2);
        when(sleepLogRepository.findByUserIdAndSleepDateBetweenOrderBySleepDateAsc(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(logs);

        ThirtyDayAveragesResponse response = sleepLogService.getLast30DayAverages(USER_ID);

        assertThat(response.getAverageTotalTimeInBedMinutes()).isEqualTo(450.0);
        assertThat(response.getAverageWentToBedAt()).isNotNull();
        assertThat(response.getAverageGotUpAt()).isNotNull();
        assertThat(response.getMorningFeelingFrequencies().get("OK")).isEqualTo(1L);
        assertThat(response.getMorningFeelingFrequencies().get("GOOD")).isEqualTo(1L);
        assertThat(response.getMorningFeelingFrequencies().get("BAD")).isEqualTo(0L);
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
