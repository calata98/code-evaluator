package com.calata.evaluator.contracts.dto;

import java.util.Map;

public record SubmitAnswersRequest(
        String testId,
        Map<String,Integer> answers // questionId -> choiceIndex
) {}
