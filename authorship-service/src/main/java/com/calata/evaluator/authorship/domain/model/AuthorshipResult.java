package com.calata.evaluator.authorship.domain.model;

import java.time.Instant;

public record AuthorshipResult(
        String submissionId,
        String language,
        double confidence,
        Verdict verdict,
        String justification,
        Instant createdAt
) {
    public static AuthorshipResult of(String submissionId, String language, double c, Verdict v, String j) {
        return new AuthorshipResult(submissionId, language, c, v, j, Instant.now());
    }
    public static AuthorshipResult heuristic(String submissionId, String language, double score) {
        Verdict v = score >= 0.8 ? Verdict.LIKELY_AUTHOR : (score >= 0.6 ? Verdict.UNCERTAIN : Verdict.LIKELY_NOT_AUTHOR);
        return of(submissionId, language, score, v, "Heuristic evaluation fallback.");
    }
}
