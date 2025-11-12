package com.calata.evaluator.submission.api.domain.model.summary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SubmissionSummary{

    private String id;
    private String userId;
    private String status;
    private String title;
    private String language;
    private Instant createdAt;


}
