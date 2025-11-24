package com.calata.evaluator.evaluation.orchestrator.application.port.out;

import com.calata.evaluator.contracts.events.ExecutionRequest;

public interface ExecutionRequester {
    void requestExecution(ExecutionRequest request);
}
