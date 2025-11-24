package com.calata.evaluator.evaluation.orchestrator.application.port.in;

import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessExecutionResultCommand;

public interface HandleExecutionResultUseCase {
    void handle(ProcessExecutionResultCommand command);
}
