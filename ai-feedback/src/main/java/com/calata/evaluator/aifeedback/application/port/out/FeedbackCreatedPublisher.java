package com.calata.evaluator.aifeedback.application.port.out;

import com.calata.evaluator.aifeedback.domain.model.Feedback;

import java.util.List;

public interface FeedbackCreatedPublisher {
    void publish(String evaluationId, String submissionId, List<Feedback> items);
}
