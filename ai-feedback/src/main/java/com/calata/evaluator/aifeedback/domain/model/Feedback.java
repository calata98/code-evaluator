package com.calata.evaluator.aifeedback.domain.model;

import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.contracts.types.Severity;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Feedback {

    private final String id;
    private final String title;
    private final String message;
    private final FeedbackType type;
    private final Severity severity;
    private final String suggestion;
    private final String reference;
    private final Instant createdAt;

    public Feedback (String title, String message, FeedbackType type, Severity severity,
              String suggestion, String reference, Instant createdAt) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.type = type;
        this.severity = severity;
        this.suggestion = suggestion;
        this.reference = reference;
        this.createdAt = createdAt;
    }

    public static Feedback of(String title, String message, FeedbackType type, Severity sev,
            String suggestion, String reference) {
        return new Feedback(title, message, type, sev, suggestion, reference, Instant.now());
    }
}
