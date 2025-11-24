package com.calata.evaluator.aifeedback.application.port.out;

import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.contracts.types.FeedbackType;

import java.util.List;
import java.util.Map;

public interface FeedbackCreatedPublisher {
    void publish(String evaluationId, String submissionId, List<Feedback> items, int score,
            Map<FeedbackType, Integer> rubric, String justification);
}
