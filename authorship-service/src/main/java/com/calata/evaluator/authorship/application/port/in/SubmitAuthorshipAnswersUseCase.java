package com.calata.evaluator.authorship.application.port.in;

import java.util.Map;

public interface SubmitAuthorshipAnswersUseCase {
    void submit(String testId, String userId, Map<String,Integer> answers);
}
