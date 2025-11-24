package com.calata.evaluator.submission.api.domain.model.summary;

import com.calata.evaluator.contracts.types.Severity;

public record FeedbackItem(
        String id,
        String title,
        String message,
        String type,
        String suggestion,
        Severity severity
) {}
