package com.calata.evaluator.aifeedback.domain.model;

import com.calata.evaluator.contracts.events.Severity;

import java.time.Instant;


public record Feedback(
        String title,
        String message,
        FeedbackType type,
        Severity severity,
        String suggestion,
        String reference,
        Instant createdAt
) {
    public static Feedback of(String title, String message, FeedbackType type, Severity sev,
            String suggestion, String reference) {
        return new Feedback(title, message, type, sev, suggestion, reference, Instant.now());
    }
}
