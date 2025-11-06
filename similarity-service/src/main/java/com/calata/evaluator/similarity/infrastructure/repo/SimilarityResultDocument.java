package com.calata.evaluator.similarity.infrastructure.repo;

import com.calata.evaluator.contracts.types.SimilarityType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("similarity_results")
@Data
public class SimilarityResultDocument {
    @Id
    private String submissionId;
    private String userId;
    private String language;
    private SimilarityType type;
    private double score;
    private String matchedSubmissionId;
    private Instant createdAt;
}
