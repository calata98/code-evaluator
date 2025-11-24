package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;

public interface AuthorshipEvaluationWriter {
    void save(AuthorshipEvaluation result);
}
