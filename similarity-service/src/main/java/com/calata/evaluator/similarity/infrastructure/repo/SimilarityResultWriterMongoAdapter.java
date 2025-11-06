package com.calata.evaluator.similarity.infrastructure.repo;

import com.calata.evaluator.similarity.application.port.out.SimilarityResultWriter;
import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import org.springframework.stereotype.Component;

@Component
public class SimilarityResultWriterMongoAdapter implements SimilarityResultWriter {

    private final SpringDataSimilarityResultRepository repo;

    public SimilarityResultWriterMongoAdapter(SpringDataSimilarityResultRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(SimilarityResult result) {
        repo.save(MongoMappers.toDocument(result));
    }
}
