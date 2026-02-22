package com.noom.interview.fullstack.sleep.controller;

import com.noom.interview.fullstack.sleep.api.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.service.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST API for sleep logs: create/update last night's log and fetch last night's sleep.
 */
@RestController
@RequestMapping("/users/{userId}/sleep-logs")
@Tag(name = "Sleep logs", description = "Create and fetch sleep log for last night")
public class SleepLogController {

    private final SleepLogService sleepLogService;

    public SleepLogController(SleepLogService sleepLogService) {
        this.sleepLogService = sleepLogService;
    }

    @Operation(summary = "Create or update sleep log", description = "Creates the sleep log for the given date (e.g. last night). One log per user per day; re-posting updates.")
    @ApiResponse(responseCode = "200", description = "Sleep log saved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SleepLogResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SleepLogResponse> createOrUpdateSleepLog(
            @PathVariable long userId,
            @Valid @RequestBody CreateSleepLogRequest request) {
        SleepLogResponse response = sleepLogService.createOrUpdateSleepLog(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get last night's sleep", description = "Returns the most recent sleep log for the user.")
    @ApiResponse(responseCode = "200", description = "Sleep log found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SleepLogResponse.class)))
    @ApiResponse(responseCode = "404", description = "No sleep log found for this user")
    @GetMapping(value = "/last", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SleepLogResponse> getLastNightSleep(@PathVariable long userId) {
        return sleepLogService.getLastNightSleep(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleUserNotFound() {
        // 404 body can be empty or a small JSON message
    }
}
