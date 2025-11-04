package com.calata.evaluator.aifeedback.infrastructure.repo;

import com.calata.evaluator.aifeedback.application.port.out.FeedbackWriter;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeedbackWriterMongoAdapter implements FeedbackWriter {

    private final SpringDataFeedbackRepository repo;

    public FeedbackWriterMongoAdapter(SpringDataFeedbackRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Feedback> saveAll(String evaluationId, List<Feedback> items) {
        var docs = items.stream().map(f -> FeedbackMongoMapper.toDoc(evaluationId, f)).toList();
        repo.saveAll(docs);
        return items;
    }

    @Override
    public boolean existsForEvaluation(String evaluationId) {
        return repo.existsByEvaluationId(evaluationId);
    }
}
