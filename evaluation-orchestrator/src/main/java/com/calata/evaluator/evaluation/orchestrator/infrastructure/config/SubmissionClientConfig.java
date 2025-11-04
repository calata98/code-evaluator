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
            @Value("${app.submission.base-url}") String baseUrl,
            @Value("${app.api.key}") String apiKey
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .filter(logErrors())
                .build();
    }

    private ExchangeFilterFunction logErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            if (resp.statusCode().isError()) {
                return resp.bodyToMono(String.class)
                        .defaultIfEmpty("<empty>")
                        .flatMap(body -> {
                            System.err.println("WebClient ERROR " + resp.statusCode() + " body=" + body);
                            return resp.createException().flatMap(Mono::error);
                        });
            }
            return Mono.just(resp);
        });
    }
}
