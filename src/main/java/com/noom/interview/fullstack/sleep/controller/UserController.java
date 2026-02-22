package com.noom.interview.fullstack.sleep.controller;

import com.noom.interview.fullstack.sleep.models.User;
import com.noom.interview.fullstack.sleep.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Minimal user API so clients can obtain a userId for sleep log endpoints.
 * Authentication is out of scope; this supports the "concept of a user".
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Create a user (get userId for sleep logs)")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "Create user", description = "Creates a new user and returns the id. Use this id for sleep log endpoints.")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> createUser() {
        User user = userRepository.save(new User());
        return Map.of("id", user.getId());
    }
}
