package com.calata.evaluator.submission.api.infrastructure.repo.mapper;

import com.calata.evaluator.submission.api.domain.model.submission.Submission;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDocument;

public final class SubmissionPersistenceMapper {

    public SubmissionPersistenceMapper() {}

    public static SubmissionDocument toDocument(Submission s) {
        return new SubmissionDocument(
                s.getId(), s.getUserId(), s.getTitle(), s.getCode(), s.getLanguage(),
                s.getStatus(), s.getCreatedAt(), s.getUpdatedAt()
        );
    }

    public static Submission toDomain(SubmissionDocument d) {
        return new Submission(
                d.getId(), d.getUserId(), d.getTitle(), d.getCode(), d.getLanguage(),
                d.getStatus(), d.getCreatedAt(), d.getUpdatedAt()
        );
    }
}
