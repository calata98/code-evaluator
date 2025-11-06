package com.calata.evaluator.authorship.application.port.in;

import com.calata.evaluator.authorship.application.command.ProcessSimilarityComputedCommand;

public interface HandleSimilarityComputedUseCase {
    void handle(ProcessSimilarityComputedCommand command);
}
