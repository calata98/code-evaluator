package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.contracts.dto.AuthorshipEvaluationView;
import com.calata.evaluator.contracts.dto.AuthorshipTestView;
import com.calata.evaluator.contracts.dto.SubmitAnswersRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AuthorshipTestProxyController {

    private final WebClient authorshipClient;

    // Authorship Test Endpoints
    @GetMapping("/authorship-tests/{id}")
    public Mono<ResponseEntity<AuthorshipTestView>> getTest(
            @PathVariable String id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        return authorshipClient.get()
                .uri("/authorship-tests/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authHeader) // relay JWT
                .retrieve()
                .toEntity(AuthorshipTestView.class);
    }

    @PostMapping("/authorship-tests/{id}/answers")
    public Mono<ResponseEntity<Void>> submitAnswers(
            @PathVariable String id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody SubmitAnswersRequest body) {

        return authorshipClient.post()
                .uri("/authorship-tests/{id}/answers", id)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity();
    }

    // Authorship Evaluation Endpoint
    @GetMapping("/authorship-evaluations/{id}")
    public Mono<ResponseEntity<AuthorshipEvaluationView>> getEvaluations(
            @PathVariable String id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        return authorshipClient.get()
                .uri("/authorship-evaluations/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authHeader) // relay JWT
                .retrieve()
                .toEntity(AuthorshipEvaluationView.class);
    }
}
