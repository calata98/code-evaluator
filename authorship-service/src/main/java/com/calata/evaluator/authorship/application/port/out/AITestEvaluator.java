package com.calata.evaluator.authorship.application.port.out;

import com.calata.evaluator.authorship.domain.model.AuthorshipAnswer;
import com.calata.evaluator.authorship.domain.model.AuthorshipResult;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;

import java.util.List;

public interface AITestEvaluator {
    AuthorshipResult evaluate(AuthorshipTest test, List<AuthorshipAnswer> answers, String truncatedCode);
}
