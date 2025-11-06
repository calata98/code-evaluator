package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipTest;

public interface AuthorshipTestWriter {
    void save(AuthorshipTest test);
}
