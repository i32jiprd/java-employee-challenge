package com.reliaquest.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Test suite for CustomResponseErrorHandler
 *
 * NOTE: We could use a parametrized test to verify all cases for hasError, but it is overkill.
 * So, I will only cover the most common ones.
 *
 * This test suite also indirectly covers TooManyRequestsException implementation
 */
@ExtendWith(MockitoExtension.class)
class CustomResponseErrorHandlerTest {

    @Mock
    ClientHttpResponse response;

    @Test
    void hasError_whenACCEPTEDHttpStatusIsFOund_returnFalse() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);

        // when
        final CustomResponseErrorHandler handler = new CustomResponseErrorHandler();
        // then
        assertFalse(handler.hasError(response));
    }

    @Test
    void hasError_whenTOO_MANY_REQUESTSHttpStatusIsFOund_returnTrue() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        // when
        final CustomResponseErrorHandler handler = new CustomResponseErrorHandler();
        // then
        assertTrue(handler.hasError(response));
    }

    @Test
    void handleError_whenHttpStatusIsTOO_MANY_REQUESTS_thenExceptionIsThrown() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        final CustomResponseErrorHandler handler = new CustomResponseErrorHandler();

        // when
        final TooManyRequestsException exception =
                assertThrows(TooManyRequestsException.class, () -> handler.handleError(response));
        // then
        assertEquals("Received too many requests", exception.getMessage());
    }

    @Test
    void handleError_whenHttpStatusIsNotTOO_MANY_REQUESTS_thenExceptionIsNotThrown() throws IOException {
        // given
        when(response.getStatusCode()).thenReturn(HttpStatus.CREATED);
        final CustomResponseErrorHandler handler = new CustomResponseErrorHandler();
        // when

        // then
        assertDoesNotThrow(() -> handler.handleError(response));
    }
}
