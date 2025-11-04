package com.calata.evaluator.aifeedback.infrastructure.repo;

import com.calata.evaluator.aifeedback.domain.model.Feedback;

public final class FeedbackMongoMapper {
    private FeedbackMongoMapper(){}

    public static FeedbackDocument toDoc(String evaluationId, Feedback f){
        return new FeedbackDocument(
                null,
                evaluationId,
                f.title(),
                f.message(),
                f.type().name(),
                f.severity().name(),
                f.suggestion(),
                f.reference(),
                f.createdAt()
        );
    }
}
