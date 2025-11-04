package com.calata.evaluator.submission.api.application.port.out;

import com.calata.evaluator.submission.api.domain.model.Submission;


public interface SubmissionWriter {
    Submission save(Submission submission);
    Submission updateStatus(String submissionId, String status);
}
