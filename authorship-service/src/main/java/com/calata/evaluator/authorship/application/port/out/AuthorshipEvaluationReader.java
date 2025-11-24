package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import reactor.core.publisher.Mono;

public interface AuthorshipEvaluationReader {
    Mono<AuthorshipEvaluation> findBySubmissionId(String id);
}
