package com.calata.evaluator.submission.api.infrastructure.web.controller;

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
@RequestMapping("/authorship-tests")
@RequiredArgsConstructor
public class AuthorshipProxyController {

    private final WebClient authorshipClient;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AuthorshipTestView>> get(
            @PathVariable String id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        return authorshipClient.get()
                .uri("/authorship-tests/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authHeader) // relay JWT
                .retrieve()
                .toEntity(AuthorshipTestView.class);
    }

    @PostMapping("/{id}/answers")
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
}
