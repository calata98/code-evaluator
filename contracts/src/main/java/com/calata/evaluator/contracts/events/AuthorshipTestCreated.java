package com.calata.evaluator.contracts.events;

import com.calata.evaluator.contracts.dto.QuestionDTO;

import java.time.Instant;
import java.util.List;

public record AuthorshipTestCreated(
        String testId,
        String submissionId,
        String userId,
        String language,
        List<QuestionDTO> questions,
        Instant expiresAt,
        Instant createdAt
) { }
