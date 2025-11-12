package com.calata.evaluator.authorship.application.service;

import com.calata.evaluator.authorship.application.port.in.GetAuthorshipTestQuery;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestReader;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.authorship.domain.service.AuthorshipPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class AuthorshipQueryService implements GetAuthorshipTestQuery {

    private final AuthorshipTestReader repo;

    @Override
    public Mono<AuthorshipTest> getForUser(String testId, String userId) {
        return repo.findById(testId)
                .switchIfEmpty(Mono.error(new RuntimeException("not-found")))
                .map(test -> {
                    AuthorshipPolicy.assertReadableBy(test, userId);
                    return test;
                });
    }
}
