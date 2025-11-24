package com.calata.evaluator.submission.api.infrastructure.repo.mapper;

import com.calata.evaluator.contracts.types.Language;
import com.calata.evaluator.submission.api.domain.model.submission.Submission;

import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionPersistenceMapperTest {

    @Test
    void toDocument_shouldCopyAllFields() {
        // given
        String id = "sub-123";
        String userId = "user-1";
        String title = "My Submission";
        String code = "print('Hello')";
        Language language = Language.PYTHON;
        SubmissionStatus status = SubmissionStatus.PENDING;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String stepName = "EXECUTION";
        String errorCode = "E001";
        String errorMessage = "Something happened";

        Submission domain = new Submission(
                id, userId, title, code, language,
                status, createdAt, updatedAt, stepName, errorCode, errorMessage
        );

        // when
        SubmissionDocument doc = SubmissionPersistenceMapper.toDocument(domain);

        // then
        assertThat(doc.getId()).isEqualTo(id);
        assertThat(doc.getUserId()).isEqualTo(userId);
        assertThat(doc.getTitle()).isEqualTo(title);
        assertThat(doc.getCode()).isEqualTo(code);
        assertThat(doc.getLanguage()).isEqualTo(language);
        assertThat(doc.getStatus()).isEqualTo(status);
        assertThat(doc.getCreatedAt()).isEqualTo(createdAt);
        assertThat(doc.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(doc.getStepName()).isEqualTo(stepName);
        assertThat(doc.getErrorCode()).isEqualTo(errorCode);
        assertThat(doc.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void toDomain_shouldCopyAllFields() {
        // given
        String id = "sub-999";
        String userId = "user-9";
        String title = "Other Submission";
        String code = "console.log('ok')";
        Language language = Language.JAVA;
        SubmissionStatus status = SubmissionStatus.RUNNING;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String stepName = "FINISHED";
        String errorCode = "E777";
        String errorMessage = "None";

        SubmissionDocument doc = new SubmissionDocument(
                id, userId, title, code, language,
                status, createdAt, updatedAt, stepName, errorCode, errorMessage
        );

        // when
        Submission domain = SubmissionPersistenceMapper.toDomain(doc);

        // then
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getTitle()).isEqualTo(title);
        assertThat(domain.getCode()).isEqualTo(code);
        assertThat(domain.getLanguage()).isEqualTo(language);
        assertThat(domain.getStatus()).isEqualTo(status);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(domain.getStepName()).isEqualTo(stepName);
        assertThat(domain.getErrorCode()).isEqualTo(errorCode);
        assertThat(domain.getErrorMessage()).isEqualTo(errorMessage);
    }
}
