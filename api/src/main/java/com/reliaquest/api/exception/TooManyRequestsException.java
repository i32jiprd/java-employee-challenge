package com.reliaquest.api.exception;

/**
 * This class is used to provide a specific Exception when the issue occurs when calling the server.
 * The exception contains a descriptive message and will help to not expose app internals
 * if it is finally thrown.
 *
 * This exception is meant to be handled by GlobalExceptionHandler
 */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
