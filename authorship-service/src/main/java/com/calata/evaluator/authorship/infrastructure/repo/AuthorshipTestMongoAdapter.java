package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.application.port.out.AuthorshipTestReader;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestWriter;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthorshipTestMongoAdapter implements AuthorshipTestReader, AuthorshipTestWriter {

    private final SpringDataAuthorshipTestRepository repo;

    public AuthorshipTestMongoAdapter(SpringDataAuthorshipTestRepository repo) {
        this.repo = repo;
    }

    @Override
    public Mono<AuthorshipTest> findByTestId(String id) {
        return repo.findBySubmissionId(id).map(Mappers::toDomain);
    }

    @Override
    public Mono<Void> save(AuthorshipTest test) {
        return repo.save(Mappers.toDocument(test)).then();
    }
}
