package com.calata.evaluator.submission.api.readmodel.model;

import com.calata.evaluator.contracts.types.FeedbackType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record EvaluationView(
        String id,
        Integer score,
        Map<FeedbackType, Integer> rubric,
        String justification,
        Instant createdAt,
        List<FeedbackItem> feedbacks
) {}
