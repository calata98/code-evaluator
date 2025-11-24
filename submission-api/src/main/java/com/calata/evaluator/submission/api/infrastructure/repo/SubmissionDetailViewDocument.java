package com.calata.evaluator.submission.api.infrastructure.repo;

import com.calata.evaluator.submission.api.domain.model.summary.EvaluationView;
import com.calata.evaluator.submission.api.domain.model.summary.SubmissionSummary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("submission_detail_view")
public record SubmissionDetailViewDocument(
        @Id String id,
        SubmissionSummary submission,
        List<EvaluationView> evaluations,
        Instant lastUpdated
) {}
