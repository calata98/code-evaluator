package com.calata.evaluator.similarity.application.port.in;

import com.calata.evaluator.similarity.application.command.ProcessEvaluationCompletedCommand;

public interface HandleEvaluationCompletedUseCase {
    void handle(ProcessEvaluationCompletedCommand command);
}
