package com.calata.evaluator.submission.api.application.port.out;

import com.calata.evaluator.submission.api.domain.model.submission.Submission;

import java.util.List;

public interface SubmissionReader {
    Submission getById(String submissionId);
    List<Submission> getByUserId(String userId);
}
