package com.calata.evaluator.evaluation.orchestrator.application.port.out;

public interface SubmissionStatusUpdater {
    void markRunning(String submissionId);
}
