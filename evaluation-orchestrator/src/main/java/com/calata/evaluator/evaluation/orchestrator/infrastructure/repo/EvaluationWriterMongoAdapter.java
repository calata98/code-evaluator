package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo;

import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.EvaluationWriter;
import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.mapper.EvaluationPersistenceMapper;
import com.mongodb.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EvaluationWriterMongoAdapter implements EvaluationWriter {

    private final SpringDataEvaluationRepository repo;
    private final MongoTemplate mongoTemplate;

    public EvaluationWriterMongoAdapter(SpringDataEvaluationRepository repo, MongoTemplate mongoTemplate) {
        this.repo = repo;
        this.mongoTemplate = mongoTemplate;
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

    @Override
    public void updateScoreAndRubricAndJustification(String evaluationId, int score,
            Map<FeedbackType, Integer> rubric, String justification) {

        Query query = new Query(Criteria.where("_id").is(evaluationId));

        boolean passed = score >= 60;

        Update update = new Update()
                .set("score", score)
                .set("rubric", rubric)      // sustituye el map completo
                .set("justification", justification)
                .set("passed", passed);

        mongoTemplate.updateFirst(query, update, EvaluationDocument.class);
    }

}
