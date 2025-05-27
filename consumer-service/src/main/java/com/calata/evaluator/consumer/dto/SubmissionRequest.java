package com.calata.evaluator.consumer.dto;

import lombok.Data;

@Data
public class SubmissionRequest {
    private String userId;
    private String exerciseId;
    private String code;
}
