package com.calata.evaluator.authorship.application.command;

import com.calata.evaluator.contracts.dto.AnswerDTO;

import java.util.List;

public record ProcessAuthorshipAnswersCommand(
        String submissionId,
        String userId,
        List<AnswerDTO> answers
) { }
