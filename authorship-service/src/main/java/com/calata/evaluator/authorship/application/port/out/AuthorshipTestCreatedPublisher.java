package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.contracts.events.AuthorshipTestCreated;

public interface AuthorshipTestCreatedPublisher {
    void publishAuthorshipTestCreated(AuthorshipTestCreated event);
}
