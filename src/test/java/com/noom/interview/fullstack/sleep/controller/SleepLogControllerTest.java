package com.noom.interview.fullstack.sleep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.models.MorningFeeling;
import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.service.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SleepLogController.class)
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
}
