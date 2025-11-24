package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo;

import com.calata.evaluator.contracts.types.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

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
    private Map<FeedbackType, Integer> rubric;
    private String justification;
    private Instant createdAt;
}
