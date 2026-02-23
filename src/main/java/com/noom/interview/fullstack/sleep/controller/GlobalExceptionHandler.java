package com.noom.interview.fullstack.sleep.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.noom.interview.fullstack.sleep.api.FieldErrorDto;
import com.noom.interview.fullstack.sleep.api.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns structured error responses for validation and bad request errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldErrorDto(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
                .collect(Collectors.toList());
        ValidationErrorResponse body = new ValidationErrorResponse(VALIDATION_FAILED_MESSAGE, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        List<FieldErrorDto> errors = List.of(new FieldErrorDto("request", ex.getMessage()));
        ValidationErrorResponse body = new ValidationErrorResponse(VALIDATION_FAILED_MESSAGE, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName() != null ? ex.getName() : "parameter";
        String message = ex.getValue() != null
                ? "invalid value: '" + ex.getValue() + "' (e.g. userId must be a valid UUID)"
                : "invalid type";
        List<FieldErrorDto> errors = List.of(new FieldErrorDto(field, message));
        ValidationErrorResponse body = new ValidationErrorResponse(VALIDATION_FAILED_MESSAGE, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        List<FieldErrorDto> errors;
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String field = ife.getPath().isEmpty() ? "body" : ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            String message = "invalid value" + (ife.getTargetType() != null && ife.getTargetType().isEnum()
                    ? "; must be one of: " + Arrays.stream(ife.getTargetType().getEnumConstants()).map(Object::toString).collect(Collectors.joining(", "))
                    : ": " + (cause.getMessage() != null ? cause.getMessage() : "invalid format"));
            errors = List.of(new FieldErrorDto(field != null ? field : "body", message));
        } else {
            String message = cause != null && cause.getMessage() != null ? cause.getMessage() : "Invalid request body";
            errors = List.of(new FieldErrorDto("body", message));
        }
        ValidationErrorResponse body = new ValidationErrorResponse(VALIDATION_FAILED_MESSAGE, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
