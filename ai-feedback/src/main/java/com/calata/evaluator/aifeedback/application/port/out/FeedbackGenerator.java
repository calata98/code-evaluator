package com.calata.evaluator.aifeedback.application.port.out;

import com.calata.evaluator.aifeedback.domain.model.FeedbackAggregate;


public interface FeedbackGenerator {
    FeedbackAggregate generateWithScore(String language, String code, String stdout, String stderr,
            long timeMs, long memoryMb);
}
