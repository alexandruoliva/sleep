package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * JPA repository for users.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
}
