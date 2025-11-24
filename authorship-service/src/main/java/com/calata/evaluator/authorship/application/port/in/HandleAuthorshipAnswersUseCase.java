package com.calata.evaluator.authorship.application.port.in;

import com.calata.evaluator.authorship.application.command.ProcessAuthorshipAnswersCommand;

public interface HandleAuthorshipAnswersUseCase {
    void handle(ProcessAuthorshipAnswersCommand command);
}
