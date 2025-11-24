package com.calata.evaluator.user.application.port.out;

import java.time.Instant;

public interface TokenStore {
    void revoke(String token, Instant expiresAt);
    boolean isRevoked(String token);
}
