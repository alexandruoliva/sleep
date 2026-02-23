package com.noom.interview.fullstack.sleep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.api.ThirtyDayAveragesResponse;
import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.service.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SleepLogController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("unittest")
class SleepLogControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SleepLogService sleepLogService;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LOG_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void createOrUpdateSleepLog_returns200AndBody() throws Exception {
        String body = "{\"sleepDate\":\"2025-02-22\",\"wentToBedAt\":\"23:00:00\",\"gotUpAt\":\"07:30:00\",\"totalTimeInBedMinutes\":510,\"morningFeeling\":\"GOOD\"}";
        SleepLogResponse response = new SleepLogResponse();
        response.setId(LOG_ID);
        response.setUserId(USER_ID);
        response.setSleepDate(LocalDate.of(2025, 2, 22));
        response.setWentToBedAt(LocalTime.of(23, 0));
        response.setGotUpAt(LocalTime.of(7, 30));
        response.setTotalTimeInBedMinutes(510);
        response.setMorningFeeling(MorningFeeling.GOOD);
        response.setCreatedAt(Instant.now());
        when(sleepLogService.createOrUpdateSleepLog(eq(USER_ID), any())).thenReturn(response);

        mockMvc.perform(post("/users/{userId}/sleep-logs", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LOG_ID.toString()))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
                .andExpect(jsonPath("$.totalTimeInBedMinutes").value(510));

        verify(sleepLogService).createOrUpdateSleepLog(eq(USER_ID), any());
    }

    @Test
    void createOrUpdateSleepLog_returns404WhenUserNotFound() throws Exception {
        String body = "{\"sleepDate\":\"2025-02-22\",\"wentToBedAt\":\"23:00\",\"gotUpAt\":\"07:30\",\"totalTimeInBedMinutes\":510,\"morningFeeling\":\"GOOD\"}";
        UUID unknownUserId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        when(sleepLogService.createOrUpdateSleepLog(eq(unknownUserId), any())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/users/{userId}/sleep-logs", unknownUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastNightSleep_returns200AndBodyWhenFound() throws Exception {
        SleepLogResponse response = new SleepLogResponse();
        response.setId(LOG_ID);
        response.setUserId(USER_ID);
        response.setSleepDate(LocalDate.of(2025, 2, 22));
        response.setMorningFeeling(MorningFeeling.OK);
        when(sleepLogService.getLastNightSleep(USER_ID)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/users/{userId}/sleep-logs/last", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LOG_ID.toString()))
                .andExpect(jsonPath("$.morningFeeling").value("OK"));

        verify(sleepLogService).getLastNightSleep(USER_ID);
    }

    @Test
    void getLastNightSleep_returns404WhenNotFound() throws Exception {
        when(sleepLogService.getLastNightSleep(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{userId}/sleep-logs/last", USER_ID))
                .andExpect(status().isNotFound());

        verify(sleepLogService).getLastNightSleep(USER_ID);
    }

    // --- 30-day averages ---

    @Test
    void getLast30DayAverages_returns200WithBody() throws Exception {
        ThirtyDayAveragesResponse response = new ThirtyDayAveragesResponse();
        response.setRangeStart(LocalDate.of(2025, 1, 25));
        response.setRangeEnd(LocalDate.of(2025, 2, 23));
        response.setAverageTotalTimeInBedMinutes(450.0);
        response.setAverageWentToBedAt(LocalTime.of(23, 15));
        response.setAverageGotUpAt(LocalTime.of(7, 0));
        Map<String, Long> frequencies = new HashMap<>();
        frequencies.put("BAD", 0L);
        frequencies.put("OK", 2L);
        frequencies.put("GOOD", 1L);
        response.setMorningFeelingFrequencies(frequencies);
        when(sleepLogService.getLast30DayAverages(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/users/{userId}/sleep-logs/30-day-averages", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rangeStart").value("2025-01-25"))
                .andExpect(jsonPath("$.rangeEnd").value("2025-02-23"))
                .andExpect(jsonPath("$.averageTotalTimeInBedMinutes").value(450.0))
                .andExpect(jsonPath("$.morningFeelingFrequencies.OK").value(2))
                .andExpect(jsonPath("$.morningFeelingFrequencies.GOOD").value(1));

        verify(sleepLogService).getLast30DayAverages(USER_ID);
    }

    @Test
    void getLast30DayAverages_returns404WhenUserNotFound() throws Exception {
        when(sleepLogService.getLast30DayAverages(USER_ID)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/users/{userId}/sleep-logs/30-day-averages", USER_ID))
                .andExpect(status().isNotFound());

        verify(sleepLogService).getLast30DayAverages(USER_ID);
    }

    // --- Validation (400) ---

    @Test
    void createOrUpdateSleepLog_returns400WithFieldErrorsWhenBodyMissingRequiredFields() throws Exception {
        String invalidBody = "{}";

        mockMvc.perform(post("/users/{userId}/sleep-logs", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(5)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("sleepDate")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("wentToBedAt")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("gotUpAt")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("totalTimeInBedMinutes")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("morningFeeling")));
    }

    @Test
    void createOrUpdateSleepLog_returns400WithFieldErrorWhenTotalTimeInBedOutOfRange() throws Exception {
        String bodyZero = "{\"sleepDate\":\"2025-02-22\",\"wentToBedAt\":\"23:00\",\"gotUpAt\":\"07:30\",\"totalTimeInBedMinutes\":0,\"morningFeeling\":\"GOOD\"}";

        mockMvc.perform(post("/users/{userId}/sleep-logs", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyZero))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field", hasItem("totalTimeInBedMinutes")))
                .andExpect(jsonPath("$.errors[0].message").value("totalTimeInBedMinutes must be between 1 and 1440"));
    }

    @Test
    void createOrUpdateSleepLog_returns400WithFieldErrorWhenInvalidMorningFeeling() throws Exception {
        String body = "{\"sleepDate\":\"2025-02-22\",\"wentToBedAt\":\"23:00\",\"gotUpAt\":\"07:30\",\"totalTimeInBedMinutes\":480,\"morningFeeling\":\"INVALID\"}";

        mockMvc.perform(post("/users/{userId}/sleep-logs", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field", hasItem("morningFeeling")));
    }

    @Test
    void createOrUpdateSleepLog_returns400WithFieldErrorWhenInvalidUserIdInPath() throws Exception {
        String body = "{\"sleepDate\":\"2025-02-22\",\"wentToBedAt\":\"23:00\",\"gotUpAt\":\"07:30\",\"totalTimeInBedMinutes\":510,\"morningFeeling\":\"GOOD\"}";

        mockMvc.perform(post("/users/not-a-uuid/sleep-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field", hasItem("userId")));
    }
}
