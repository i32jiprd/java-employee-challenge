package com.reliaquest.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConfigurationProperties(prefix = "webclientconfig")
public class WebClientConfig {
    private String baseurl;

    public String getBaseurl() {
        return baseurl;
    }

    public void setBaseurl(String baseurl) {
        this.baseurl = baseurl;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(baseurl).build();
    }
}
