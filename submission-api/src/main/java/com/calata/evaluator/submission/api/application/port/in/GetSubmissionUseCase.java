package com.calata.evaluator.submission.api.application.port.in;

import com.calata.evaluator.contracts.dto.SubmissionResponse;

import java.util.List;

public interface GetSubmissionUseCase {
    SubmissionResponse getSubmission(String submissionId);
    List<SubmissionResponse> getSubmissionByUserId(String userId);
}
