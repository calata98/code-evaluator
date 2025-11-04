package com.calata.evaluator.contracts.events;

import java.time.Instant;
import java.util.List;

public record FeedbackCreated(
        String evaluationId,
        String submissionId,
        List<Item> items,
        Instant createdAt
) {
    public static FeedbackCreated of(String evaluationId, String submissionId, List<Item> list){
        var items = list.stream().map(f ->
                new Item(f.title(), f.message(), f.type(), f.severity(), f.suggestion(), f.reference())
        ).toList();
        return new FeedbackCreated(evaluationId, submissionId, items, Instant.now());
    }
    public record Item(String title, String message, String type, Severity severity, String suggestion, String reference){}
}
