package com.calata.evaluator.evaluation.orchestrator.application.command;

import com.calata.evaluator.contracts.types.FeedbackType;

import java.util.Map;

public record ProcessAIFeedbackCreatedCommand(
        String evaluationId,
        int score,
        Map<FeedbackType, Integer> rubric,
        String justification
) {}
