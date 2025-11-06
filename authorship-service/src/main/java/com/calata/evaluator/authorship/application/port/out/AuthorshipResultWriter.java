package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipResult;

public interface AuthorshipResultWriter {
    void save(AuthorshipResult result);
}
