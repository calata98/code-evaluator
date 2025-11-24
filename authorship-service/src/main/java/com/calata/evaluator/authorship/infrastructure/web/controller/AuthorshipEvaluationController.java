package com.calata.evaluator.authorship.infrastructure.web.controller;

import com.calata.evaluator.authorship.application.port.in.GetAuthorshipEvaluationQuery;
import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import com.calata.evaluator.contracts.dto.AuthorshipEvaluationView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/authorship-evaluations")
@RequiredArgsConstructor
public class AuthorshipEvaluationController {

    private final GetAuthorshipEvaluationQuery evaluationQuery;

    @GetMapping("/{submissionId}")
    public Mono<ResponseEntity<AuthorshipEvaluationView>> getEvaluationBySubmissionId(
            @PathVariable String submissionId) {
        return evaluationQuery.findBySubmissionId(submissionId)
                .map(this::toView)
                .map(ResponseEntity::ok);
    }

    private AuthorshipEvaluationView toView(AuthorshipEvaluation r) {
        return new AuthorshipEvaluationView(
               r.confidence(), r.verdict().name(), r.justification(), r.createdAt()
        );
    }
}
