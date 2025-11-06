package com.calata.evaluator.aifeedback.domain.service;

import com.calata.evaluator.contracts.types.Severity;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FeedbackSynthesisService {
    public List<Feedback> validateAndNormalize(List<Feedback> items) {
        return items.stream()
                .map(f -> f.getSeverity()==null ? new Feedback(
                        f.getTitle(), f.getMessage(), f.getType(), Severity.MINOR,
                        f.getSuggestion(), f.getReference(), f.getCreatedAt()) : f)
                .distinct().toList();
    }
}
