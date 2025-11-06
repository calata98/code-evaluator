package com.calata.evaluator.contracts.events;

import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.contracts.types.Severity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AIFeedbackCreated(
        String evaluationId,
        String submissionId,
        List<Item> items,
        Instant createdAt,
        int score,
        Map<FeedbackType, Integer> rubric,
        String justification
) {
    public static AIFeedbackCreated of(String evaluationId, String submissionId, List<Item> list){
        var items = list.stream().map(f ->
                new Item(f.id(), f.title(), f.message(), f.type(), f.severity(), f.suggestion(), f.reference())
        ).toList();
        return new AIFeedbackCreated(evaluationId, submissionId, items, Instant.now(), 0, Map.of(), null);
    }

    public record Item(
            String id,
            String title,
            String message,
            String type,
            Severity severity,
            String suggestion,
            String reference
    ){}
}
