package com.calata.evaluator.contracts.events;

import java.util.List;

public record AIFeedbackRequested(
        String evaluationId,
        String submissionId,
        String code,
        String language,
        List<String> types,
        Integer maxItemsPerType,
        String stdout,
        String stderr,
        long timeMs,
        long memoryMb
) {}
