package com.calata.evaluator.submission.api.readmodel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("submission_detail_view")
public record SubmissionDetailViewDocument(
        @Id String id, // submissionId
        SubmissionSummary submission,
        List<EvaluationView> evaluations,
        Instant lastUpdated
) {}
