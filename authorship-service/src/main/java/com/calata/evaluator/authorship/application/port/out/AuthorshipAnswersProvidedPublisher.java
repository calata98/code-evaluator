package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.contracts.events.AuthorshipAnswersProvided;

public interface AuthorshipAnswersProvidedPublisher {
    void publishAuthorshipAnswersProvided(AuthorshipAnswersProvided answersProvided);
}
