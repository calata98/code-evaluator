package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.mapper;

import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.EvaluationDocument;

public final class EvaluationPersistenceMapper {
    private EvaluationPersistenceMapper(){}

    public static EvaluationDocument toDocument(Evaluation e) {
        return new EvaluationDocument(
                e.getId(), e.getSubmissionId(), e.isPassed(), e.getScore(),
                e.getRubric(), e.getJustification(),
                e.getCreatedAt()
        );
    }

    public static Evaluation toDomain(EvaluationDocument d) {
        return new Evaluation(
                d.getId(), d.getSubmissionId(), d.isPassed(), d.getScore(),
                d.getRubric(), d.getJustification(),
                d.getCreatedAt()
        );
    }
}
