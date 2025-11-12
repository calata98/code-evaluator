package com.calata.evaluator.submission.api.application.port.out;

import com.calata.evaluator.submission.api.domain.model.submission.Submission;


public interface SubmissionWriter {
    Submission save(Submission submission);
    Submission updateStatus(String submissionId, String status);
}
