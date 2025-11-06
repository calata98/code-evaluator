package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipTest;

import java.util.Optional;

public interface AuthorshipTestReader {
    Optional<AuthorshipTest> findBySubmission(String submissionId);
}
