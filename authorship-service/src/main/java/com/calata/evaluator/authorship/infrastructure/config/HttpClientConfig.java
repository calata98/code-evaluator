package com.calata.evaluator.authorship.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfig {

    @Bean
    WebClient submissionWebClient(
            @Value("${SUBMISSIONS_API_URL:http://localhost:8080}") String baseUrl,
            @Value("${INTERNAL_API_KEY}") String apiKey
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .build();
    }
}
