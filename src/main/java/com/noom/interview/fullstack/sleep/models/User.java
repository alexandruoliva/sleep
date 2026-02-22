package com.noom.interview.fullstack.sleep.models;

import java.time.Instant;

/**
 * Represents a user. The API is aware of the concept of a user;
 * authentication/authorization is out of scope.
 */
public final class User {

    private final long id;
    private final Instant createdAt;

    public User(long id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
