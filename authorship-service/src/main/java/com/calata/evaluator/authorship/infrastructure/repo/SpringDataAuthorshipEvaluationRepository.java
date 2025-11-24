package com.calata.evaluator.authorship.infrastructure.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;


public interface SpringDataAuthorshipEvaluationRepository extends
        ReactiveMongoRepository<AuthorshipEvaluationDocument, String> {
    Mono<AuthorshipEvaluationDocument> findBySubmissionId(String submissionId);
}

