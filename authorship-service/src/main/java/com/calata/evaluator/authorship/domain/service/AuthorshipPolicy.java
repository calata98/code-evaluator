package com.calata.evaluator.authorship.domain.service;

import com.calata.evaluator.authorship.domain.model.AuthorshipTest;

import java.time.Instant;

public class AuthorshipPolicy {
    public static void assertReadableBy(AuthorshipTest test, String userId) {
        if (!test.userId().equals(userId)) {
            throw new SecurityException("not owner");
        }

        if (test.expiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("expired");
        }
    }
}
