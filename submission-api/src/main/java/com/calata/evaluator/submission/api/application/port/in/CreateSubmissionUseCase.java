package com.calata.evaluator.submission.api.application.port.in;

import com.calata.evaluator.submission.api.application.command.CreateSubmissionCommand;

public interface CreateSubmissionUseCase {
    String handle(CreateSubmissionCommand cmd);
}
