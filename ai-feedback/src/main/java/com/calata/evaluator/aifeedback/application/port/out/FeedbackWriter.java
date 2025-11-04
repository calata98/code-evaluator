package com.calata.evaluator.aifeedback.application.port.out;

import com.calata.evaluator.aifeedback.domain.model.Feedback;
import java.util.List;

public interface FeedbackWriter {
    List<Feedback> saveAll(String evaluationId, List<Feedback> items);
    boolean existsForEvaluation(String evaluationId);
}
