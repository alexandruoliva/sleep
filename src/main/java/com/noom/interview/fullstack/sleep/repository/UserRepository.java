package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for users.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
