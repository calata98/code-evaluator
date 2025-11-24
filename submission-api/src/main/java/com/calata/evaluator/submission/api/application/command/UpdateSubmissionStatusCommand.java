package com.calata.evaluator.submission.api.application.command;

public record UpdateSubmissionStatusCommand(
    String submissionId,
    String status
) {}
