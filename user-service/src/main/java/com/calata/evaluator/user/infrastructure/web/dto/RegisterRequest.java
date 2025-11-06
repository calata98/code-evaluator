package com.calata.evaluator.user.infrastructure.web.dto;

public record RegisterRequest(
        String email,
        String password,
        String role
) {}
