package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo;

import com.calata.evaluator.evaluation.orchestrator.application.port.out.EvaluationWriter;
import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.mapper.EvaluationPersistenceMapper;
import com.mongodb.DuplicateKeyException;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EvaluationWriterMongoAdapter implements EvaluationWriter {

    private final SpringDataEvaluationRepository repo;

    public EvaluationWriterMongoAdapter(SpringDataEvaluationRepository repo) {
        this.repo = repo;
    }

    @Override
    public Evaluation save(Evaluation evaluation) {
        var existing = repo.findBySubmissionId(evaluation.getSubmissionId());
        if (existing.isPresent()) {
            return EvaluationPersistenceMapper.toDomain(existing.get());
        }
        try {
            EvaluationDocument toSave = EvaluationPersistenceMapper.toDocument(evaluation);
            EvaluationDocument saved = repo.save(toSave);
            return EvaluationPersistenceMapper.toDomain(saved);
        } catch (DuplicateKeyException e) {
            return repo.findBySubmissionId(evaluation.getSubmissionId())
                    .map(EvaluationPersistenceMapper::toDomain)
                    .orElseThrow(() -> e);
        }
    }
}
