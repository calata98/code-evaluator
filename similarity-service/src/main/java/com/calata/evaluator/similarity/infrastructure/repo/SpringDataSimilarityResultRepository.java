package com.calata.evaluator.similarity.infrastructure.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataSimilarityResultRepository extends MongoRepository<SimilarityResultDocument, String> {}
