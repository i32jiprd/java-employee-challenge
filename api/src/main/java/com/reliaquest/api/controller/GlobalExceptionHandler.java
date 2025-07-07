package com.reliaquest.api.controller;

import com.reliaquest.api.exception.TooManyRequestsException;
import jakarta.validation.ConstraintViolationException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * This class provides a general control over how some exceptions are displayed after a failed API request.
 * The main goal is to prevent internal implementation details to be exposed when an exception occurs.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * This handler deals on situations where we provide an incorrect method argument.
     *
     * @param ex - MethodArgumentNotValidException
     * @return - A descriptive exception text from the exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * This handler deals on situations where we provide a value violating a constrain.
     * Ex. When we try to create an Employee and provide an out of range age value.
     *
     * @param ex - ConstraintViolationException
     * @return - A descriptive exception text from the exception
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations()
                .forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * This handler deals on situations where we provide an incorrect parameter or value in a request.
     *
     * @param ex - IllegalArgumentException
     * @return - A descriptive exception text from the exception
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * This handler deals on situations where the server is not running or the connection is severed.
     *
     * @param ex - ConnectException
     * @return - A descriptive exception text from the exception
     */
    @ExceptionHandler({ConnectException.class})
    public ResponseEntity<String> handleConnectException(ConnectException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    /**
     * This handler deals on situations where the server is not accepting more request and the retry mechanism is unable to cope with it.
     *
     * @param ex - TooManyRequestsException
     * @return - A descriptive exception text from the exception
     */
    @ExceptionHandler({TooManyRequestsException.class})
    public ResponseEntity<String> handleTooManyRequestsException(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }
}
