package com.noom.interview.fullstack.sleep.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single field validation error in a 400 response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class FieldErrorDto {

    private String field;
    private String message;
}
