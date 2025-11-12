package com.calata.evaluator.authorship.application.port.in;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface SubmitAuthorshipAnswersUseCase {
    Mono<Void> submit(String testId, String userId, Map<String,Integer> answers);
}
