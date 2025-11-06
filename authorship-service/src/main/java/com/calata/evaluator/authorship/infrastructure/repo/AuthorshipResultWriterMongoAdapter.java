package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.application.port.out.AuthorshipResultWriter;
import com.calata.evaluator.authorship.domain.model.AuthorshipResult;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipResultWriterMongoAdapter implements AuthorshipResultWriter {

    private final SpringDataAuthorshipResultRepository repo;

    public AuthorshipResultWriterMongoAdapter(SpringDataAuthorshipResultRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(AuthorshipResult result) {
        repo.save(MongoMappers.toDocument(result));
    }
}
