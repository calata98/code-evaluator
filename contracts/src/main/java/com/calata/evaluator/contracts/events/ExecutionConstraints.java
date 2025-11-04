package com.calata.evaluator.contracts.events;

import java.time.Duration;

public record ExecutionConstraints(
        Duration timeout,
        Long memoryLimitMb,
        Integer cpuShares
) {
    public static ExecutionConstraints defaults() {
        return new ExecutionConstraints(Duration.ofSeconds(5), 256L, 1);
    }
}

