package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataEvaluationRepository extends MongoRepository<EvaluationDocument, String> {
    Optional<EvaluationDocument> findBySubmissionId(String submissionId);
}
