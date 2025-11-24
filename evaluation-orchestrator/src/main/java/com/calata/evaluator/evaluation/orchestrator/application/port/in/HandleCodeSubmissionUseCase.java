package com.calata.evaluator.evaluation.orchestrator.application.port.in;

import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessSubmissionCreatedCommand;

public interface HandleCodeSubmissionUseCase {
    void handle(ProcessSubmissionCreatedCommand command);
}
