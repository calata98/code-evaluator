package com.calata.evaluator.authorship.application.port.in;

import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import reactor.core.publisher.Mono;

public interface GetAuthorshipEvaluationQuery {
    Mono<AuthorshipEvaluation> findBySubmissionId(String testId);
}
