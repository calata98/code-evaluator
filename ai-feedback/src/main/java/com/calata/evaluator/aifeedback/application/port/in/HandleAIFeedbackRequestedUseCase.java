package com.calata.evaluator.aifeedback.application.port.in;

import com.calata.evaluator.aifeedback.application.command.ProcessAIFeedbackRequestedCommand;

public interface HandleAIFeedbackRequestedUseCase {
    void handle(ProcessAIFeedbackRequestedCommand command);
}
