package com.calata.evaluator.submission.api.application.command;

import com.calata.evaluator.contracts.types.Language;

public record CreateSubmissionCommand (
    String userId,
    String title,
    String code,
    Language language
) {}
