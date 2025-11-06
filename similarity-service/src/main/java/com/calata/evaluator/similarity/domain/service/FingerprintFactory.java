package com.calata.evaluator.similarity.domain.service;

import com.calata.evaluator.similarity.domain.model.Fingerprint;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FingerprintFactory {

    private final NormalizationService normalization;

    public FingerprintFactory(NormalizationService normalization) {
        this.normalization = normalization;
    }

    public Fingerprint create(String submissionId, String userId, String language, String code, Instant at) {
        String shaRaw = sha256(code);
        String norm = normalization.normalize(code, language);
        String shaNorm = sha256(norm);
        long simhash = simhash64(ngrams(norm, 4));
        int lines = (int) code.lines().count();
        return new Fingerprint(submissionId, userId, language, shaRaw, shaNorm, simhash, lines, at);
    }

    private static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    private static List<String> ngrams(String text, int n) {
        String[] toks = text.split("\\s+");
        List<String> out = new ArrayList<>();
        for (int i = 0; i + n <= toks.length; i++) {
            out.add(String.join(" ", Arrays.copyOfRange(toks, i, i + n)));
        }
        return out;
    }

    private static long simhash64(List<String> tokens) {
        long[] v = new long[64];
        for (String t : tokens) {
            long h = simple64(t);
            for (int i = 0; i < 64; i++) v[i] += ((h >>> i) & 1L) == 1L ? 1 : -1;
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
