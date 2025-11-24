package com.calata.evaluator.evaluation.orchestrator.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class SubmissionClientConfig {

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
