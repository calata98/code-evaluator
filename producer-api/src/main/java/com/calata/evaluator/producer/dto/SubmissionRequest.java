package com.calata.evaluator.producer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmissionRequest {

    private String userId;
    private String exerciseId;

    @NotBlank(message = "Language cannot be blank")
    private String language;

    @NotBlank(message = "Code cannot be blank")
    private String code;

}
