package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.application.port.out.AuthorshipTestWriter;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipTestWriterMongoAdapter implements AuthorshipTestWriter {

    private final SpringDataAuthorshipTestRepository repo;

    public AuthorshipTestWriterMongoAdapter(SpringDataAuthorshipTestRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(AuthorshipTest test) {
        repo.save(MongoMappers.toDocument(test));
    }
}
