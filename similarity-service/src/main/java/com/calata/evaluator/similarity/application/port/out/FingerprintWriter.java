package com.calata.evaluator.similarity.application.port.out;

import com.calata.evaluator.similarity.domain.model.Fingerprint;

public interface FingerprintWriter {
    void upsert(Fingerprint fp);
}
