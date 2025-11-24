package com.calata.evaluator.submission.api.infrastructure.web.dto;

import com.calata.evaluator.contracts.types.Language;
import jakarta.validation.constraints.NotBlank;

public record SubmissionRequest (
        @NotBlank(message = "Language cannot be blank")
        Language language,

        @NotBlank(message = "Title cannot be null")
        String title,

        @NotBlank(message = "Code cannot be blank")
        String code){

}
