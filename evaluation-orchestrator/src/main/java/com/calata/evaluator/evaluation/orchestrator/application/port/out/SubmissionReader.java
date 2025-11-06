package com.calata.evaluator.evaluation.orchestrator.application.port.out;

public interface SubmissionReader {
    SubmissionSnapshot findById(String submissionId);

    record SubmissionSnapshot(
            String id,
            String language,
            String code,
            String userId
    ) {}
}
