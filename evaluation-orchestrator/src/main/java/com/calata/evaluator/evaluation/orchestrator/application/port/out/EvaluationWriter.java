package com.calata.evaluator.evaluation.orchestrator.application.port.out;

import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;

public interface EvaluationWriter {
    Evaluation save(Evaluation evaluation);
}
