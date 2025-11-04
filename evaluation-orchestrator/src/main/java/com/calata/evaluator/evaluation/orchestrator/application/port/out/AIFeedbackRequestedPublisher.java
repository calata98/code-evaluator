package com.calata.evaluator.evaluation.orchestrator.application.port.out;

public interface AIFeedbackRequestedPublisher {
    void publish(String evaluationId,
            String submissionId,
            String language,
            String code);
}
