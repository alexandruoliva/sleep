package com.noom.interview.fullstack.sleep.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response body for 400 Bad Request when request validation fails.
 * Tells the client what is wrong with the request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class ValidationErrorResponse {

    private String message;
    private List<FieldErrorDto> errors;
}
