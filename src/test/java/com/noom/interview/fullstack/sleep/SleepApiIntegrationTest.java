package com.noom.interview.fullstack.sleep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: full application context, real HTTP calls to main endpoints.
 * Uses in-memory H2 (unittest profile); no external PostgreSQL required.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("unittest")
class SleepApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullFlow_createUser_createSleepLog_getLast_get30DayAverages() {
        // 1. Create user
        ResponseEntity<Map> createUserResponse = restTemplate.postForEntity("/users", null, Map.class);
        assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createUserResponse.getBody()).containsKey("id");
        String userId = String.valueOf(createUserResponse.getBody().get("id"));

        // 2. Create sleep log
        String sleepLogBody = "{\n"
                + "  \"sleepDate\": \"2025-02-22\",\n"
                + "  \"wentToBedAt\": \"23:00\",\n"
                + "  \"gotUpAt\": \"07:30\",\n"
                + "  \"totalTimeInBedMinutes\": 510,\n"
                + "  \"morningFeeling\": \"GOOD\"\n"
                + "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> createLogResponse = restTemplate.postForEntity(
                "/users/" + userId + "/sleep-logs",
                new HttpEntity<>(sleepLogBody, headers),
                Map.class);
        assertThat(createLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createLogResponse.getBody()).isNotNull();
        assertThat(createLogResponse.getBody().get("sleepDate")).isEqualTo("2025-02-22");
        assertThat(createLogResponse.getBody().get("morningFeeling")).isEqualTo("GOOD");
        assertThat(createLogResponse.getBody().get("totalTimeInBedMinutes")).isEqualTo(510);

        // 3. Get last night's sleep
        ResponseEntity<Map> lastResponse = restTemplate.getForEntity(
                "/users/" + userId + "/sleep-logs/last",
                Map.class);
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lastResponse.getBody()).isNotNull();
        assertThat(lastResponse.getBody().get("sleepDate")).isEqualTo("2025-02-22");
        assertThat(lastResponse.getBody().get("morningFeeling")).isEqualTo("GOOD");

        // 4. Get 30-day averages
        ResponseEntity<Map> averagesResponse = restTemplate.getForEntity(
                "/users/" + userId + "/sleep-logs/30-day-averages",
                Map.class);
        assertThat(averagesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(averagesResponse.getBody()).isNotNull();
        assertThat(averagesResponse.getBody()).containsKeys("rangeStart", "rangeEnd", "averageTotalTimeInBedMinutes", "morningFeelingFrequencies");
        assertThat(averagesResponse.getBody().get("morningFeelingFrequencies")).isNotNull();
    }

    @Test
    void getLastNightSleep_returns404WhenNoLog() {
        ResponseEntity<Map> createUserResponse = restTemplate.postForEntity("/users", null, Map.class);
        assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String userId = String.valueOf(createUserResponse.getBody().get("id"));

        ResponseEntity<Void> lastResponse = restTemplate.getForEntity(
                "/users/" + userId + "/sleep-logs/last",
                Void.class);
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSleepLog_returns400WithValidationErrorsWhenInvalidBody() {
        ResponseEntity<Map> createUserResponse = restTemplate.postForEntity("/users", null, Map.class);
        assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String userId = String.valueOf(createUserResponse.getBody().get("id"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/users/" + userId + "/sleep-logs",
                new HttpEntity<>("{}", headers),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Validation failed");
        assertThat(response.getBody().get("errors")).isInstanceOf(List.class);
        List<?> errors = (List<?>) response.getBody().get("errors");
        assertThat(errors).hasSize(5);
    }

    @Test
    void sleepLogEndpoints_returns404WhenUserNotFound() {
        String unknownUserId = "11111111-1111-1111-1111-111111111111";
        String body = "{\"sleepDate\":\"2025-02-22\",\"wentToBedAt\":\"23:00\",\"gotUpAt\":\"07:30\",\"totalTimeInBedMinutes\":510,\"morningFeeling\":\"GOOD\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> postResponse = restTemplate.postForEntity(
                "/users/" + unknownUserId + "/sleep-logs",
                new HttpEntity<>(body, headers),
                Void.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Void> getAveragesResponse = restTemplate.getForEntity(
                "/users/" + unknownUserId + "/sleep-logs/30-day-averages",
                Void.class);
        assertThat(getAveragesResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
