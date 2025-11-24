package com.calata.evaluator.evaluation.orchestrator.application.port.out;

import com.calata.evaluator.contracts.events.EvaluationCreated;

public interface EvaluationCreatedPublisher {
    void publish(EvaluationCreated event);
}
