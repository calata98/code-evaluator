package com.calata.evaluator.authorship.application.service;

import com.calata.evaluator.authorship.application.port.in.SubmitAuthorshipAnswersUseCase;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestReader;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestWriter;
import com.calata.evaluator.authorship.domain.service.AuthorshipPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthorshipAnswerService implements SubmitAuthorshipAnswersUseCase {

    private final AuthorshipTestReader reader;
    private final AuthorshipTestWriter writer;

    @Override
    public Mono<Void> submit(String testId, String userId, Map<String, Integer> answers) {
        return reader.findById(testId)
                .switchIfEmpty(Mono.error(new RuntimeException("not-found")))
                .flatMap(test -> {
                    AuthorshipPolicy.assertReadableBy(test, userId);
                    // int score = test.score(answers);
                    // test.markAnswered();
                    return writer.save(test);
                });
    }
}
