package com.calata.evaluator.similarity.application.port.out;

import com.calata.evaluator.similarity.domain.model.SimilarityResult;

public interface SimilarityResultWriter {
    void save(SimilarityResult result);
}
