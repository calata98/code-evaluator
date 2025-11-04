package com.calata.evaluator.evaluation.orchestrator.domain.service;

import org.springframework.stereotype.Service;

@Service
public class ScoringService {
    public int calculateScore(long timeMs, long memoryMb) {
        int timePenalty = (int)Math.min(timeMs / 100, 20);
        int memPenalty  = (int)Math.min(memoryMb / 32, 10);
        return Math.max(0, 10 - timePenalty - memPenalty);
    }
}
