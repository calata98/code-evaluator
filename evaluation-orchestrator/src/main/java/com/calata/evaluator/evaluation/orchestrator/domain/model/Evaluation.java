package com.calata.evaluator.evaluation.orchestrator.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class Evaluation {
    private String id;
    private String submissionId;
    private boolean passed;
    private int score;
    private long timeMs;
    private long memoryMb;
    private Instant createdAt;

    private Evaluation() {}

    public static Evaluation createForSubmission(
            String submissionId, int score,
            long timeMs, long memoryMb) {
        Evaluation e = new Evaluation();
        e.submissionId = submissionId;
        e.passed = score >= 5;
        e.score = score;
        e.timeMs = timeMs;
        e.memoryMb = memoryMb;
        e.createdAt = Instant.now();
        return e;
    }


}

