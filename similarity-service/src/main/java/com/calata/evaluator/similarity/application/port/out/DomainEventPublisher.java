package com.calata.evaluator.similarity.application.port.out;

import com.calata.evaluator.similarity.domain.model.SimilarityResult;

public interface DomainEventPublisher {
    void publishSimilarityComputed(SimilarityResult result);
}
