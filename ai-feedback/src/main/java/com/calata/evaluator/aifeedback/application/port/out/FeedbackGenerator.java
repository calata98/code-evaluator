package com.calata.evaluator.aifeedback.application.port.out;

import com.calata.evaluator.aifeedback.domain.model.Feedback;
import java.util.List;

public interface FeedbackGenerator {
    List<Feedback> generate(String language, String code);
}
