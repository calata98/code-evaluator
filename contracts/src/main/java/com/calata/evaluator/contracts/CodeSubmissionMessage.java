package com.calata.evaluator.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CodeSubmissionMessage {

    private String submissionId;
    private String language;
    private String code;
    private LocalDateTime timestamp;

}
