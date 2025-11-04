package com.calata.evaluator.aifeedback.domain.service;

import com.calata.evaluator.aifeedback.domain.model.*;
import com.calata.evaluator.contracts.events.Severity;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FeedbackSynthesisService {
    public List<Feedback> validateAndNormalize(List<Feedback> items) {
        // reglas simples: limitar longitud, normalizar severidades, eliminar duplicados
        return items.stream()
                .map(f -> f.severity()==null ? new Feedback(
                        f.title(), f.message(), f.type(), Severity.MINOR,
                        f.suggestion(), f.reference(), f.createdAt()) : f)
                .distinct().toList();
    }
}
