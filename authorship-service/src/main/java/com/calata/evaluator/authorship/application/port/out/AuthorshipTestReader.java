package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import reactor.core.publisher.Mono;

public interface AuthorshipTestReader {
    Mono<AuthorshipTest> findByTestId(String id);
}
