package com.calata.evaluator.user.infrastructure.web.dto;

public record UserResponse(
        String id,
        String email,
        String role,
        String createdAt
) {}
