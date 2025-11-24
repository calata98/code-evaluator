package com.calata.evaluator.authorship.domain.model;

import java.time.Instant;

public record AuthorshipEvaluation(
        String submissionId,
        String userId,
        String language,
        double confidence,
        Verdict verdict,
        String justification,
        Instant createdAt
) {
    public static AuthorshipEvaluation of(String submissionId, String userId, String language, double confidence, Verdict verdict, String justification) {
        return new AuthorshipEvaluation(submissionId, userId, language, confidence, verdict, justification, Instant.now());
    }
    public static AuthorshipEvaluation heuristic(String submissionId, String userId, String language, double score) {
        Verdict verdict = score >= 0.8 ? Verdict.LIKELY_AUTHOR : (score >= 0.6 ? Verdict.UNCERTAIN : Verdict.LIKELY_NOT_AUTHOR);
        return of(submissionId, userId, language, score, verdict, "Heuristic evaluation fallback.");
    }
}
