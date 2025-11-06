package com.calata.evaluator.user.infrastructure.web.dto;

public record LoginRequest(
        String email,
        String password
) {}
