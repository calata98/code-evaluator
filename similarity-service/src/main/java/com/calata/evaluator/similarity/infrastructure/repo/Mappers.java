package com.calata.evaluator.similarity.infrastructure.repo;

import com.calata.evaluator.contracts.events.SimilarityComputed;
import com.calata.evaluator.contracts.types.SimilarityType;
import com.calata.evaluator.similarity.domain.model.Fingerprint;
import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import com.calata.evaluator.similarity.domain.model.SimilarityTypeDomain;

import java.util.List;
import java.util.Optional;

public final class Mappers {
    private Mappers() {}

    // Fingerprint
    public static FingerprintDocument toDocument(Fingerprint fp) {
        var d = new FingerprintDocument();
        d.setSubmissionId(fp.submissionId());
        d.setUserId(fp.userId());
        d.setLanguage(fp.language());
        d.setShaRaw(fp.shaRaw());
        d.setShaNorm(fp.shaNorm());
        d.setSimhash64(fp.simhash64());
        d.setLineCount(fp.lineCount());
        d.setCreatedAt(fp.createdAt());
        return d;
    }

    public static Optional<Fingerprint> fingerprintToDomain(Optional<FingerprintDocument> optionalFingerprintDocument) {

        return optionalFingerprintDocument.map(d -> new Fingerprint(
                d.getSubmissionId(),
                d.getUserId(),
                d.getLanguage(),
                d.getShaRaw(),
                d.getShaNorm(),
                d.getSimhash64(),
                d.getLineCount(),
                d.getCreatedAt()
        ));
    }

    public static List<Fingerprint> fingerprintsToDomain(List<FingerprintDocument> documents) {
        return documents.stream().map(d -> new Fingerprint(
                d.getSubmissionId(),
                d.getUserId(),
                d.getLanguage(),
                d.getShaRaw(),
                d.getShaNorm(),
                d.getSimhash64(),
                d.getLineCount(),
                d.getCreatedAt()
        )).toList();
    }

    // Similarity
    public static SimilarityResultDocument toDocument(SimilarityResult sr) {
        var d = new SimilarityResultDocument();
        d.setSubmissionId(sr.submissionId());
        d.setUserId(sr.userId());
        d.setLanguage(sr.language());
        d.setType(toContractsType(sr.type()));
        d.setScore(sr.score());
        d.setMatchedSubmissionId(sr.matchedSubmissionId());
        d.setCreatedAt(sr.createdAt());
        return d;
    }

    public static SimilarityComputed toEvent(SimilarityResult sr) {
        return new SimilarityComputed(
                sr.submissionId(),
                sr.userId(),
                sr.language(),
                sr.code(),
                toContractsType(sr.type()),
                sr.score(),
                sr.matchedSubmissionId(),
                sr.createdAt()
        );
    }

    private static SimilarityType toContractsType(SimilarityTypeDomain t) {
        return switch (t) {
            case EXACT -> SimilarityType.EXACT;
            case NORMALIZED -> SimilarityType.NORMALIZED;
            case NEAR -> SimilarityType.NEAR;
            case NONE -> SimilarityType.NONE;
        };
    }
}
