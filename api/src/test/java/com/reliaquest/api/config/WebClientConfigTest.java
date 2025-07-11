package com.reliaquest.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Test suite to check WebClientConfig configuration
 */
@SpringBootTest
class WebClientConfigTest {

    @Autowired
    private WebClient webClient;

    @Autowired
    private WebClientConfig config;

    @Test
    void webClientBaseUrlShouldBeSetFromConfig() {
        assertEquals("http://localhost:8112/api/v1/employee", config.getBaseurl());
    }
}
