package com.calata.evaluator.authorship.domain.model;

import java.util.List;

public record AuthorshipQuestion(
        String id,
        String prompt,
        List<String> choices,
        Integer correctIndexHint
) { }
