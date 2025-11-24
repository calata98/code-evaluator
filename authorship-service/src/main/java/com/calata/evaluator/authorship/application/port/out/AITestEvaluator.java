package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipAnswer;
import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;

import java.util.List;

public interface AITestEvaluator {
    AuthorshipEvaluation evaluate(AuthorshipTest test, List<AuthorshipAnswer> answers, String truncatedCode);
}
