package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.TooManyRequestsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Test suite for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    public static final String FIELD_NAME = "fieldName";
    public static final String FIELD_IS_REQUIRED = "Field is required";
    public static final String MUST_BE_18 = "must be >= 18";
    public static final String AGE = "age";
    public static final String INVALID_PARAMETER = "Invalid parameter";
    public static final String CONNECTION_REFUSED = "Connection refused";
    public static final String TOO_MANY_REQUESTS = "Too many requests";

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleValidationException_returnsMessageFromFieldError() {
        // given
        final MethodParameter methodParameter = mock(MethodParameter.class);
        final BindingResult bindingResult = mock(BindingResult.class);
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        final FieldError fieldError = new FieldError("objectName", FIELD_NAME, FIELD_IS_REQUIRED);

        // when
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        final ResponseEntity<Map<String, String>> response = handler.handleValidationException(ex);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(FIELD_IS_REQUIRED, response.getBody().get(FIELD_NAME));
    }

    @Test
    void handleConstraintViolation_returnsMessage() {
        // given
        final ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        final Path path = mock(Path.class);

        final ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        // when
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(AGE);
        when(violation.getMessage()).thenReturn(MUST_BE_18);
        final ResponseEntity<Map<String, String>> response = handler.handleConstraintViolation(ex);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MUST_BE_18, response.getBody().get(AGE));
    }

    @Test
    void handleIllegalArgument_returnsMessage() {
        // given
        final IllegalArgumentException ex = new IllegalArgumentException(INVALID_PARAMETER);

        // when
        final ResponseEntity<String> response = handler.handleIllegalArgument(ex);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(INVALID_PARAMETER, response.getBody());
    }

    @Test
    void handleConnectException_returnsMessage() {
        // given
        final ConnectException ex = new ConnectException(CONNECTION_REFUSED);

        // when
        final ResponseEntity<String> response = handler.handleConnectException(ex);

        // then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(CONNECTION_REFUSED, response.getBody());
    }

    @Test
    void handleTooManyRequestsException_returnsMessage() {
        // given
        final TooManyRequestsException ex = new TooManyRequestsException(TOO_MANY_REQUESTS);

        // when
        final ResponseEntity<String> response = handler.handleTooManyRequestsException(ex);

        // then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(TOO_MANY_REQUESTS, response.getBody());
    }

}
