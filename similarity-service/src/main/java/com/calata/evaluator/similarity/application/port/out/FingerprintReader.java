package com.calata.evaluator.similarity.application.port.out;

import com.calata.evaluator.similarity.domain.model.Fingerprint;

import java.util.List;
import java.util.Optional;

public interface FingerprintReader {
    Optional<Fingerprint> findByShaRaw(String shaRaw);
    Optional<Fingerprint> findByShaNorm(String shaNorm);
    List<Fingerprint> findRecentByLangAndSize(String lang, int lineCount, int limit, double tolerance);
}
