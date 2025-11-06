package com.calata.evaluator.similarity.infrastructure.repo;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataFingerprintRepository extends MongoRepository<FingerprintDocument, String> {

    Optional<FingerprintDocument> findFirstByShaRaw(String shaRaw);

    Optional<FingerprintDocument> findFirstByShaNorm(String shaNorm);

    @Query("{ 'language': ?0, 'lineCount': { $gte: ?1, $lte: ?2 } }")
    List<FingerprintDocument> findByLangAndCountBetween(String lang, int min, int max, Sort sort);
}
