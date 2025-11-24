package com.calata.evaluator.evaluation.orchestrator.application.port.out;

import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;

import java.util.Map;

public interface EvaluationWriter {
    Evaluation save(Evaluation evaluation);
    void updateScoreAndRubricAndJustification(String evaluationId, int score,
            Map<FeedbackType, Integer> rubric, String justification);
}
