package com.calata.evaluator.submission.api.domain.model;

import com.calata.evaluator.contracts.types.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import static com.calata.evaluator.submission.api.domain.model.SubmissionStatus.PENDING;

@AllArgsConstructor
@Getter
public class Submission {
    private String id;
    private String userId;
    private String title;
    private String code;
    private Language language;
    private SubmissionStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static Submission create(String userId, String title, String code, Language language) {
        return new Submission(UUID.randomUUID().toString(), userId, title, code, language, PENDING, Instant.now(), Instant.now());
    }
}
