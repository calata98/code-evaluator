package com.calata.evaluator.authorship.infrastructure.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataAuthorshipTestRepository extends MongoRepository<AuthorshipTestDocument, String> {
    Optional<AuthorshipTestDocument> findBySubmissionId(String submissionId);
}
