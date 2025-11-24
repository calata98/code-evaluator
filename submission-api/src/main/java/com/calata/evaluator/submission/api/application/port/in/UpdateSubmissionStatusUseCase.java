package com.calata.evaluator.submission.api.application.port.in;

public interface UpdateSubmissionStatusUseCase {
    String updateSubmissionStatus(String submissionId, String status);
}
