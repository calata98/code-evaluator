package com.calata.evaluator.authorship.application.service;

import com.calata.evaluator.authorship.application.port.in.SubmitAuthorshipAnswersUseCase;
import com.calata.evaluator.authorship.application.port.out.AuthorshipAnswersProvidedPublisher;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestReader;
import com.calata.evaluator.authorship.infrastructure.repo.Mappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthorshipAnswerService implements SubmitAuthorshipAnswersUseCase {

    private final AuthorshipTestReader reader;
    private final AuthorshipAnswersProvidedPublisher publisher;

    @Override
    public void submit(String submissionId, String userId, Map<String, Integer> answers) {
       publisher.publishAuthorshipAnswersProvided(Mappers.toEvent(submissionId, userId, answers));
    }
}
