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

    private static final long USER_ID = 1L;

    @Test
    void createOrUpdateSleepLog_returns200AndBody() throws Exception {
        String body = """
                {"sleepDate":"2025-02-22","wentToBedAt":"23:00:00","gotUpAt":"07:30:00","totalTimeInBedMinutes":510,"morningFeeling":"GOOD"}
                """;
        SleepLogResponse response = new SleepLogResponse();
        response.setId(10L);
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
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
                .andExpect(jsonPath("$.totalTimeInBedMinutes").value(510));

        verify(sleepLogService).createOrUpdateSleepLog(eq(USER_ID), any());
    }

    @Test
    void createOrUpdateSleepLog_returns404WhenUserNotFound() throws Exception {
        String body = """
                {"sleepDate":"2025-02-22","wentToBedAt":"23:00","gotUpAt":"07:30","totalTimeInBedMinutes":510,"morningFeeling":"GOOD"}
                """;
        when(sleepLogService.createOrUpdateSleepLog(eq(999L), any())).thenThrow(new UserNotFoundException("User not found: 999"));

        mockMvc.perform(post("/users/999/sleep-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastNightSleep_returns200AndBodyWhenFound() throws Exception {
        SleepLogResponse response = new SleepLogResponse();
        response.setId(5L);
        response.setUserId(USER_ID);
        response.setSleepDate(LocalDate.of(2025, 2, 22));
        response.setMorningFeeling(MorningFeeling.OK);
        when(sleepLogService.getLastNightSleep(USER_ID)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/users/{userId}/sleep-logs/last", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
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
