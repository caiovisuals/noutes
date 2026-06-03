package com.noutes.config;

import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorBody(int status, String error, Object message, Instant timestamp) {}

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorBody> handle(ResponseStatusException ex) {
        return body(ex.getStatusCode().value(),
                HttpStatus.resolve(ex.getStatusCode().value()) != null
                        ? HttpStatus.resolve(ex.getStatusCode().value()).getReasonPhrase()
                        : "Error",
                ex.getReason() != null ? ex.getReason() : ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorBody> handle(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "invalid"));
        return body(HttpStatus.BAD_REQUEST.value(), "Validation Failed", fields);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorBody> handle(BadCredentialsException ex) {
        return body(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", "Invalid email or password");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorBody> handle(Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage());
    }

    private ResponseEntity<ErrorBody> body(int status, String error, Object message) {
        return ResponseEntity.status(status)
                .body(new ErrorBody(status, error, message, Instant.now()));
    }
}