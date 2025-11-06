package com.calata.evaluator.submission.api.readmodel.repo;

import com.calata.evaluator.submission.api.readmodel.model.SubmissionDetailViewDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubmissionDetailViewRepository extends MongoRepository<SubmissionDetailViewDocument, String> {

    List<SubmissionDetailViewDocument> findBySubmissionUserIdOrderBySubmissionCreatedAtDesc(
            String userId, Pageable pageable);
}
