package com.calata.evaluator.submission.api.infrastructure.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataSubmissionRepository extends MongoRepository<SubmissionDocument, String> {
    List<SubmissionDocument> findByUserId(String userId);
}
