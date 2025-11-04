package com.calata.evaluator.user.application.port.out;

import com.calata.evaluator.user.domain.model.User;

import java.time.Instant;

public interface TokenEncoder {
    String encode(User user, Instant expiresAt);
    boolean isValid(String token);
    String subject(String token); // userId
    Instant expiresAt(String token);
}
