package com.calata.evaluator.contracts.dto;

public record SubmissionResponse(
        String id,
        String title,
        String language,
        String code,
        String status,
        String createdAt
) {}
