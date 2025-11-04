package com.calata.evaluator.submission.api.application.port.out;

import com.calata.evaluator.submission.api.domain.model.Submission;

public interface SubmissionEventsPublisher {
    void publishCodeSubmission(Submission submission);
}
