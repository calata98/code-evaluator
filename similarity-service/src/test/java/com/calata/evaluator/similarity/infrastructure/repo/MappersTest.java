package com.calata.evaluator.similarity.infrastructure.repo;

import static org.junit.jupiter.api.Assertions.*;

import com.calata.evaluator.contracts.events.SimilarityComputed;
import com.calata.evaluator.contracts.types.SimilarityType;
import com.calata.evaluator.similarity.domain.model.Fingerprint;
import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import com.calata.evaluator.similarity.domain.model.SimilarityTypeDomain;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

class MappersTest {

    // ---------- Fingerprint: toDocument ----------

    @Test
    void toDocument_shouldMapFingerprintFieldsCorrectly() {
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");

        Fingerprint fp = new Fingerprint(
                "sub-1",
                "user-1",
                "JAVA",
                "sha-raw",
                "sha-norm",
                123456789L,
                42,
                createdAt
        );

        FingerprintDocument doc = Mappers.toDocument(fp);

        assertEquals("sub-1", doc.getSubmissionId());
        assertEquals("user-1", doc.getUserId());
        assertEquals("JAVA", doc.getLanguage());
        assertEquals("sha-raw", doc.getShaRaw());
        assertEquals("sha-norm", doc.getShaNorm());
        assertEquals(123456789L, doc.getSimhash64());
        assertEquals(42, doc.getLineCount());
        assertEquals(createdAt, doc.getCreatedAt());
    }

    // ---------- Fingerprint: fingerprintToDomain ----------

    @Test
    void fingerprintToDomain_shouldMapPresentOptionalToFingerprint() {
        Instant createdAt = Instant.parse("2024-01-02T11:30:00Z");

        FingerprintDocument doc = new FingerprintDocument();
        doc.setSubmissionId("sub-2");
        doc.setUserId("user-2");
        doc.setLanguage("PYTHON");
        doc.setShaRaw("raw-2");
        doc.setShaNorm("norm-2");
        doc.setSimhash64(987654321L);
        doc.setLineCount(100);
        doc.setCreatedAt(createdAt);

        Optional<Fingerprint> resultOpt = Mappers.fingerprintToDomain(Optional.of(doc));

        assertTrue(resultOpt.isPresent());
        Fingerprint fp = resultOpt.get();
        assertEquals("sub-2", fp.submissionId());
        assertEquals("user-2", fp.userId());
        assertEquals("PYTHON", fp.language());
        assertEquals("raw-2", fp.shaRaw());
        assertEquals("norm-2", fp.shaNorm());
        assertEquals(987654321L, fp.simhash64());
        assertEquals(100, fp.lineCount());
        assertEquals(createdAt, fp.createdAt());
    }

    @Test
    void fingerprintToDomain_shouldReturnEmptyWhenOptionalEmpty() {
        Optional<Fingerprint> resultOpt = Mappers.fingerprintToDomain(Optional.empty());
        assertTrue(resultOpt.isEmpty());
    }

    // ---------- Fingerprint: fingerprintsToDomain ----------

    @Test
    void fingerprintsToDomain_shouldMapListOfDocumentsToFingerprints() {
        Instant createdAt1 = Instant.parse("2024-01-03T10:00:00Z");
        Instant createdAt2 = Instant.parse("2024-01-03T11:00:00Z");

        FingerprintDocument d1 = new FingerprintDocument();
        d1.setSubmissionId("sub-1");
        d1.setUserId("user-1");
        d1.setLanguage("JAVA");
        d1.setShaRaw("raw1");
        d1.setShaNorm("norm1");
        d1.setSimhash64(1L);
        d1.setLineCount(10);
        d1.setCreatedAt(createdAt1);

        FingerprintDocument d2 = new FingerprintDocument();
        d2.setSubmissionId("sub-2");
        d2.setUserId("user-2");
        d2.setLanguage("PYTHON");
        d2.setShaRaw("raw2");
        d2.setShaNorm("norm2");
        d2.setSimhash64(2L);
        d2.setLineCount(20);
        d2.setCreatedAt(createdAt2);

        List<Fingerprint> result = Mappers.fingerprintsToDomain(List.of(d1, d2));

        assertEquals(2, result.size());

        Fingerprint fp1 = result.get(0);
        assertEquals("sub-1", fp1.submissionId());
        assertEquals("user-1", fp1.userId());
        assertEquals("JAVA", fp1.language());
        assertEquals("raw1", fp1.shaRaw());
        assertEquals("norm1", fp1.shaNorm());
        assertEquals(1L, fp1.simhash64());
        assertEquals(10, fp1.lineCount());
        assertEquals(createdAt1, fp1.createdAt());

        Fingerprint fp2 = result.get(1);
        assertEquals("sub-2", fp2.submissionId());
        assertEquals("user-2", fp2.userId());
        assertEquals("PYTHON", fp2.language());
        assertEquals("raw2", fp2.shaRaw());
        assertEquals("norm2", fp2.shaNorm());
        assertEquals(2L, fp2.simhash64());
        assertEquals(20, fp2.lineCount());
        assertEquals(createdAt2, fp2.createdAt());
    }

    // ---------- SimilarityResult: toDocument ----------

    @Test
    void toDocument_shouldMapSimilarityResultFieldsCorrectly() {
        Instant createdAt = Instant.parse("2024-01-04T09:00:00Z");

        SimilarityResult sr = new SimilarityResult(
                "sub-1",
                "user-1",
                "JAVA",
                "class X {}",
                SimilarityTypeDomain.EXACT,
                0.95,
                "other-sub",
                createdAt
        );

        SimilarityResultDocument doc = Mappers.toDocument(sr);

        assertEquals("sub-1", doc.getSubmissionId());
        assertEquals("user-1", doc.getUserId());
        assertEquals("JAVA", doc.getLanguage());
        assertEquals(SimilarityType.EXACT, doc.getType());
        assertEquals(0.95, doc.getScore());
        assertEquals("other-sub", doc.getMatchedSubmissionId());
        assertEquals(createdAt, doc.getCreatedAt());
    }

    // ---------- SimilarityResult: toEvent ----------

    @Test
    void toEvent_shouldMapSimilarityResultToSimilarityComputedEvent() {
        Instant createdAt = Instant.parse("2024-01-05T12:00:00Z");

        SimilarityResult sr = new SimilarityResult(
                "sub-2",
                "user-2",
                "PYTHON",
                "print('hi')",
                SimilarityTypeDomain.NEAR,
                0.75,
                "matched-sub",
                createdAt
        );

        SimilarityComputed ev = Mappers.toEvent(sr);

        assertEquals("sub-2", ev.submissionId());
        assertEquals("user-2", ev.userId());
        assertEquals("PYTHON", ev.language());
        assertEquals("print('hi')", ev.code());
        assertEquals(SimilarityType.NEAR, ev.type());
        assertEquals(0.75, ev.score());
        assertEquals("matched-sub", ev.matchedSubmissionId());
        assertEquals(createdAt, ev.createdAt());
    }

    // ---------- SimilarityType mapping coverage ----------

    @Test
    void toDocument_shouldMapAllSimilarityTypeDomainValues() {
        Instant now = Instant.parse("2024-01-06T10:00:00Z");

        for (SimilarityTypeDomain t : SimilarityTypeDomain.values()) {
            SimilarityResult sr = new SimilarityResult(
                    "sub",
                    "user",
                    "LANG",
                    "code",
                    t,
                    0.5,
                    "other",
                    now
            );

            SimilarityResultDocument doc = Mappers.toDocument(sr);

            SimilarityType expected = switch (t) {
                case EXACT -> SimilarityType.EXACT;
                case NORMALIZED -> SimilarityType.NORMALIZED;
                case NEAR -> SimilarityType.NEAR;
                case NONE -> SimilarityType.NONE;
            };

            assertEquals(expected, doc.getType());
        }
    }

    @Test
    void toEvent_shouldMapAllSimilarityTypeDomainValues() {
        Instant now = Instant.parse("2024-01-06T11:00:00Z");

        for (SimilarityTypeDomain t : SimilarityTypeDomain.values()) {
            SimilarityResult sr = new SimilarityResult(
                    "sub",
                    "user",
                    "LANG",
                    "code",
                    t,
                    0.5,
                    "other",
                    now
            );

            SimilarityComputed ev = Mappers.toEvent(sr);

            SimilarityType expected = switch (t) {
                case EXACT -> SimilarityType.EXACT;
                case NORMALIZED -> SimilarityType.NORMALIZED;
                case NEAR -> SimilarityType.NEAR;
                case NONE -> SimilarityType.NONE;
            };

            assertEquals(expected, ev.type());
        }
    }
}
