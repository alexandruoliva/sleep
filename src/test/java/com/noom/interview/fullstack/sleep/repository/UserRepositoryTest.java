package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("unittest")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_returnsUserWithGeneratedId() {
        User user = userRepository.save(new User());
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        assertThat(userRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findById_returnsUserAfterSave() {
        User created = userRepository.save(new User());
        assertThat(userRepository.findById(created.getId())).isPresent();
        assertThat(userRepository.findById(created.getId()).get().getId()).isEqualTo(created.getId());
    }
}
