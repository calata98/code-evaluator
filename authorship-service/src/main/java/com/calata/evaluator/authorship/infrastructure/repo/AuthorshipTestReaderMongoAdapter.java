package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.application.port.out.AuthorshipTestReader;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthorshipTestReaderMongoAdapter implements AuthorshipTestReader {

    private final SpringDataAuthorshipTestRepository repo;

    public AuthorshipTestReaderMongoAdapter(SpringDataAuthorshipTestRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<AuthorshipTest> findBySubmission(String submissionId) {
        return repo.findBySubmissionId(submissionId).map(MongoMappers::fromDocument);
    }
}
