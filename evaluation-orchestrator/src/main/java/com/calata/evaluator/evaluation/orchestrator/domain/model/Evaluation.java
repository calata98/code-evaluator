package com.calata.evaluator.evaluation.orchestrator.domain.model;

import com.calata.evaluator.contracts.types.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
public class Evaluation {
    private String id;
    private String submissionId;
    private boolean passed;
    private Integer score;
    private Map<FeedbackType, Integer> rubric;
    private String justification;
    private Instant createdAt;

    private Evaluation() {}

    public static Evaluation createForSubmission(
            String submissionId, int score, Map<FeedbackType, Integer> rubric, String justification) {
        Evaluation e = new Evaluation();
        e.submissionId = submissionId;
        e.passed = score >= 5;
        e.score = score;
        e.rubric = rubric;
        e.justification = justification;
        e.createdAt = Instant.now();
        return e;
    }


}

