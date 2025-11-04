package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("evaluations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String submissionId;

    private boolean passed;
    private int score;
    private long timeMs;
    private long memoryMb;
    private Instant createdAt;
}
