package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.application.port.out.AuthorshipTestReader;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestWriter;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class AuthorshipTestMongoAdapter implements AuthorshipTestReader, AuthorshipTestWriter {

    private final SpringDataAuthorshipTestRepository repo;

    public AuthorshipTestMongoAdapter(SpringDataAuthorshipTestRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<AuthorshipTest> findBySubmission(String submissionId) {
        return repo.findBySubmissionId(submissionId)
                .map(MongoMappers::fromDocument)
                .blockOptional();
    }

    @Override
    public Mono<AuthorshipTest> findById(String id) {
        return repo.findById(id).map(MongoMappers::fromDocument);
    }

    @Override
    public Mono<Void> save(AuthorshipTest test) {
        return repo.save(MongoMappers.toDocument(test)).then();
    }
}
