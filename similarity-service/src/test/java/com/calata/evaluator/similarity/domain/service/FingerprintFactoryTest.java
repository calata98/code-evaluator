package com.calata.evaluator.similarity.domain.service;

import com.calata.evaluator.similarity.domain.model.Fingerprint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FingerprintFactoryTest {

    private NormalizationService normalization;
    private FingerprintFactory factory;

    @BeforeEach
    void setUp() {
        normalization = mock(NormalizationService.class);
        factory = new FingerprintFactory(normalization);
    }

    @Test
    void create_generatesCorrectFingerprint() throws Exception {
        // given
        String submissionId = "sub-1";
        String userId = "u-1";
        String lang = "java";
        String code = "int a = 1;\nint b = 2;";
        Instant at = Instant.parse("2025-11-23T10:15:00Z");

        // mock normalization
        String normalized = "int a 1 int b 2";
        when(normalization.normalize(code, lang)).thenReturn(normalized);

        // expected sha256 raw
        String expectedShaRaw = sha256(code);

        // expected sha256 normalized
        String expectedShaNorm = sha256(normalized);

        // expected line count
        int expectedLines = (int) code.lines().count();

        // expected simhash
        long expectedSimhash = computeSimhash(normalized);

        // when
        Fingerprint fp = factory.create(submissionId, userId, lang, code, at);

        // then
        assertEquals(submissionId, fp.submissionId());
        assertEquals(userId, fp.userId());
        assertEquals(lang, fp.language());
        assertEquals(expectedShaRaw, fp.shaRaw());
        assertEquals(expectedShaNorm, fp.shaNorm());
        assertEquals(expectedSimhash, fp.simhash64());
        assertEquals(expectedLines, fp.lineCount());
        assertEquals(at, fp.createdAt());
    }

    // --------------------
    // Helper methods
    // (replican exactamente la lógica de los privados del factory)
    // --------------------

    private static String sha256(String s) throws Exception {
        var md = MessageDigest.getInstance("SHA-256");
        var dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static long computeSimhash(String normalized) {
        var toks = normalized.split("\\s+");
        java.util.List<String> ngrams = new java.util.ArrayList<>();
        for (int i = 0; i + 4 <= toks.length; i++) {
            ngrams.add(String.join(" ", java.util.Arrays.copyOfRange(toks, i, i + 4)));
        }
        return simhash64(ngrams);
    }

    private static long simhash64(java.util.List<String> tokens) {
        long[] v = new long[64];
        for (String t : tokens) {
            long h = simple64(t);
            for (int i = 0; i < 64; i++) {
                v[i] += ((h >>> i) & 1L) == 1L ? 1 : -1;
            }
        }
        long r = 0L;
        for (int i = 0; i < 64; i++) if (v[i] > 0) r |= (1L << i);
        return r;
    }

    private static long simple64(String key) {
        long h = 1125899906842597L;
        for (int i = 0; i < key.length(); i++) h = 31 * h + key.charAt(i);
        return h;
    }
}
