package com.noom.interview.fullstack.sleep.controller;

import com.noom.interview.fullstack.sleep.models.User;
import com.noom.interview.fullstack.sleep.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("unittest")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void createUser_returns200AndJsonWithId() throws Exception {
        User savedUser = new User(USER_ID, Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID.toString()));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_returnsValidUuidInResponse() throws Exception {
        User savedUser = new User(USER_ID, Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String responseBody = mockMvc.perform(post("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Response should be parseable as JSON with "id" key
        org.assertj.core.api.Assertions.assertThat(responseBody).contains(USER_ID.toString());
    }
}
