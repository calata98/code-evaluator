package com.calata.evaluator.user.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(
        String id,
        String email,
        String passwordHash,
        Role role,
        Instant createdAt
) {

    public static User newUser(String email, String passwordHash, Role role) {
        return new User(UUID.randomUUID().toString(), email.toLowerCase(), passwordHash, role, Instant.now());
    }
}
