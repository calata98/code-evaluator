package com.calata.evaluator.contracts.events;

import com.calata.evaluator.contracts.dto.AnswerDTO;

import java.util.List;

public record AuthorshipAnswersProvided(
        String submissionId,
        String userId,
        List<AnswerDTO> answers
) {}
