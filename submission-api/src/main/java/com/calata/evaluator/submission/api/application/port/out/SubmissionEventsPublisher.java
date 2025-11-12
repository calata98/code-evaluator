package com.calata.evaluator.submission.api.application.port.out;

import com.calata.evaluator.submission.api.domain.model.submission.Submission;

public interface SubmissionEventsPublisher {
    void publishSubmission(Submission submission);
    void publishSubmissionStatusUpdated(Submission submission);
}
