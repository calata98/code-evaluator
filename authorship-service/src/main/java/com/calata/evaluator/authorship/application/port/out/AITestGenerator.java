package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipTest;

public interface AITestGenerator {
    AuthorshipTest generate(String submissionId, String language, String truncatedCode, String suspicionSummary);
}
