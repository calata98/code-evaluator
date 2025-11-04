package com.calata.evaluator.aifeedback.infrastructure.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataFeedbackRepository extends MongoRepository<FeedbackDocument, String> {
    boolean existsByEvaluationId(String evaluationId);
}
