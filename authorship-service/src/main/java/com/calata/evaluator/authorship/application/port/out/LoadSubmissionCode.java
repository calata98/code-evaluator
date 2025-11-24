package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.contracts.dto.SubmissionCodeResponse;

public interface LoadSubmissionCode {
    SubmissionCodeResponse loadById(String submissionId);
}
