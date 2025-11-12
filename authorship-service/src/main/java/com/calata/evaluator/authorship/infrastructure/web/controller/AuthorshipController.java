package com.calata.evaluator.authorship.infrastructure.web.controller;

import com.calata.evaluator.authorship.application.port.in.GetAuthorshipTestQuery;
import com.calata.evaluator.authorship.application.port.in.SubmitAuthorshipAnswersUseCase;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.contracts.dto.AuthorshipTestView;
import com.calata.evaluator.contracts.dto.SubmitAnswersRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/authorship-tests")
@RequiredArgsConstructor
public class AuthorshipController {

    private final GetAuthorshipTestQuery authorshipTestQuery;
    private final SubmitAuthorshipAnswersUseCase answersUseCase;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AuthorshipTestView>> get(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        return authorshipTestQuery.getForUser(id, userId)
                .map(this::toView)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/answers")
    public Mono<ResponseEntity<Void>> submit(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SubmitAnswersRequest body) {
        var userId = jwt.getSubject();
        return answersUseCase.submit(id, userId, body.answers())
                .thenReturn(ResponseEntity.noContent().build());
    }

    private AuthorshipTestView toView(AuthorshipTest t) {
        return new AuthorshipTestView(
                t.testId(), t.submissionId(), t.language(), t.createdAt(), t.expiresAt(),
                t.questions().stream()
                        .map(q -> new AuthorshipTestView.Question(q.id(), q.prompt(), q.choices()))
                        .toList()
        );
    }
}
