package com.noom.interview.fullstack.sleep.service;

/**
 * Thrown when an operation references a user that does not exist.
 */
public final class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
