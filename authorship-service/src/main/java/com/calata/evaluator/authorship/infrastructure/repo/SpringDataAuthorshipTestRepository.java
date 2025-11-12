package com.calata.evaluator.authorship.infrastructure.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SpringDataAuthorshipTestRepository extends ReactiveMongoRepository<AuthorshipTestDocument, String> {
    Mono<AuthorshipTestDocument> findBySubmissionId(String submissionId);
}
