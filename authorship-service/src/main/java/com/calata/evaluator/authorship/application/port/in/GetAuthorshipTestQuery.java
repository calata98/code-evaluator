package com.calata.evaluator.authorship.application.port.in;

import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import reactor.core.publisher.Mono;

public interface GetAuthorshipTestQuery {
    Mono<AuthorshipTest> getTestForUser(String testId, String userId);
}
