package com.calata.evaluator.user.application.port.out;

import com.calata.evaluator.user.domain.model.User;

import java.util.Optional;

public interface UserReader {
    Optional<User> findByEmail(String email);
    Optional<User> findById(String id);
}
