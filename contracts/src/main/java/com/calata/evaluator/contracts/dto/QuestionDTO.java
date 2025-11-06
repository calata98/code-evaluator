package com.calata.evaluator.contracts.dto;

import java.util.List;

public record QuestionDTO(
        String id,
        String prompt,
        List<String> choices,
        Integer correctIndexHint
) { }
