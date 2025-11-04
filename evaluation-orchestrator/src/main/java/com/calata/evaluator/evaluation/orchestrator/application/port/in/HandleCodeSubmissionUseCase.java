package com.calata.evaluator.evaluation.orchestrator.application.port.in;

import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessCodeSubmissionCommand;

public interface HandleCodeSubmissionUseCase {
    void handle(ProcessCodeSubmissionCommand command);
}
