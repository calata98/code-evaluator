package com.calata.evaluator.evaluation.orchestrator.application.port.in;

import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessAIFeedbackCreatedCommand;

public interface HandleAIFeedbackCreatedUseCase {
    void handle(ProcessAIFeedbackCreatedCommand command);
}
