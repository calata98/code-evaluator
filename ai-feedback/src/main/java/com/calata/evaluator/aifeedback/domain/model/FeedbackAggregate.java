package com.calata.evaluator.aifeedback.domain.model;

import com.calata.evaluator.contracts.types.FeedbackType;

import java.util.List;
import java.util.Map;

public record FeedbackAggregate(
        int score,
        Map<FeedbackType, Integer> rubric,
        String justification,
        List<Feedback> items
) {}
