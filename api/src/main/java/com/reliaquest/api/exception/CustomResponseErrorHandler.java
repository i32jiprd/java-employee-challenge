package com.reliaquest.api.exception;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * This handler class deals with the TOO_MANY_REQUESTS exception from the server.
 * This exception will randomly occur when performing calls to the server.
 *
 * This handler work along with the @Retryable annotation to try to make the
 * Application more resilient to that situation.
 */
public class CustomResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return (response.getStatusCode().is4xxClientError());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
            throw new TooManyRequestsException("Received too many requests");
        }
    }
}
