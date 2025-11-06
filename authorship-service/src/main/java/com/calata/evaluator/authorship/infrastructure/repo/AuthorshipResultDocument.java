package com.calata.evaluator.authorship.infrastructure.repo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("authorship_results")
@Data
public class AuthorshipResultDocument {
    @Id
    private String submissionId;
    private String language;
    private double confidence;
    private String verdict;
    private String justification;
    private Instant createdAt;
}
