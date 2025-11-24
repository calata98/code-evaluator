package com.calata.evaluator.submission.api.infrastructure.repo;

import com.calata.evaluator.contracts.types.Language;
import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("submissions")
@CompoundIndexes({
        @CompoundIndex(name="ix_user_createdAt", def="{ 'userId': 1, 'createdAt': -1 }"),
        @CompoundIndex(name="ix_exercise_createdAt", def="{ 'exerciseId': 1, 'createdAt': -1 }")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubmissionDocument {
    @Id
    private String id;

    @Indexed
    private String userId;
    private String title;
    private String code;
    private Language language;

    @Indexed
    private SubmissionStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String stepName;
    private String errorCode;
    private String errorMessage;

}
