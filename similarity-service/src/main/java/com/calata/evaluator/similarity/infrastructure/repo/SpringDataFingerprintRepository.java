package com.calata.evaluator.similarity.infrastructure.repo;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataFingerprintRepository extends MongoRepository<FingerprintDocument, String> {

    Optional<FingerprintDocument> findFirstByShaRawAndUserIdNot(String shaRaw, String userId);

    Optional<FingerprintDocument> findFirstByShaNormAndUserIdNot(String shaNorm, String userId);

    @Query("{ 'language': ?0, 'lineCount': { $gte: ?1, $lte: ?2 }, 'userId':  { $ne:  ?3}}")
    List<FingerprintDocument> findByLangAndCountBetween(String lang, int min, int max, String userId, Sort sort);
}
