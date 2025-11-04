package com.calata.evaluator.evaluation.orchestrator.application.command;

public record ProcessExecutionResultCommand(
        String submissionId,
        String stdout,
        String stderr,
        long timeMs,
        long memoryMb
) {}
