package com.calata.evaluator.user.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


@Getter
@EqualsAndHashCode
public final class User {
    private final String id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final Instant createdAt;

    public User(String id, String email, String passwordHash, Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User newUser(String email, String passwordHash, Role role) {
        return new User(UUID.randomUUID().toString(), email.toLowerCase(), passwordHash, role, Instant.now());
    }

    public User withPasswordHash(String newHash) {
        return new User(this.id, this.email, newHash, this.role, this.createdAt);
    }
}
