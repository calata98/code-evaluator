package com.calata.evaluator.producer.dto;

import lombok.Data;

@Data
public class SubmissionRequest {

    private String userId;
    private String exerciseId;
    private String code;

}
