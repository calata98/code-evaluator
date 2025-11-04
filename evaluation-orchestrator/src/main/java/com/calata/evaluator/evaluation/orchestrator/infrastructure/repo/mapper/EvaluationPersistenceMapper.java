package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.mapper;

import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.EvaluationDocument;

public final class EvaluationPersistenceMapper {
    private EvaluationPersistenceMapper(){}

    public static EvaluationDocument toDocument(Evaluation e) {
        return new EvaluationDocument(
                e.getId(), e.getSubmissionId(), e.isPassed(), e.getScore(),
                e.getTimeMs(), e.getMemoryMb(),
                e.getCreatedAt()
        );
    }

    public static Evaluation toDomain(EvaluationDocument d) {
        return new Evaluation(
                d.getId(), d.getSubmissionId(), d.isPassed(), d.getScore(),
                d.getTimeMs(), d.getMemoryMb(),
                d.getCreatedAt()
        );
    }
}
