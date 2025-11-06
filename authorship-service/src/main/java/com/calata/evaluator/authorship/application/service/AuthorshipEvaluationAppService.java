package com.calata.evaluator.authorship.application.service;

import com.calata.evaluator.authorship.application.command.ProcessAuthorshipAnswersCommand;
import com.calata.evaluator.authorship.application.port.in.HandleAuthorshipAnswersUseCase;
import com.calata.evaluator.authorship.application.port.out.AITestEvaluator;
import com.calata.evaluator.authorship.application.port.out.AuthorshipResultWriter;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestReader;
import com.calata.evaluator.authorship.application.port.out.AuthorshipResultComputedPublisher;
import com.calata.evaluator.authorship.domain.model.AuthorshipAnswer;
import com.calata.evaluator.authorship.domain.model.AuthorshipResult;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.authorship.domain.service.QuizGrader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorshipEvaluationAppService implements HandleAuthorshipAnswersUseCase {

    private final AuthorshipTestReader testReader;
    private final AuthorshipResultWriter resultWriter;
    private final AuthorshipResultComputedPublisher publisher;
    private final AITestEvaluator evaluator;
    private final QuizGrader grader;

    public AuthorshipEvaluationAppService(AuthorshipTestReader testReader,
            AuthorshipResultWriter resultWriter,
            AuthorshipResultComputedPublisher publisher,
            AITestEvaluator evaluator,
            QuizGrader grader) {
        this.testReader = testReader;
        this.resultWriter = resultWriter;
        this.publisher = publisher;
        this.evaluator = evaluator;
        this.grader = grader;
    }

    @Override
    public void handle(ProcessAuthorshipAnswersCommand cmd) {
        AuthorshipTest test = testReader.findBySubmission(cmd.submissionId())
                .orElseThrow(() -> new IllegalStateException("AuthorshipTest not found for submission " + cmd.submissionId()));

        List<AuthorshipAnswer> answers = cmd.answers().stream()
                .map(a -> new AuthorshipAnswer(a.questionId(), a.text()))
                .toList();

        AuthorshipResult result = evaluator.evaluate(test, answers, null);
        if (result == null) {
            double s = grader.heuristicScore(test, answers);
            result = AuthorshipResult.heuristic(cmd.submissionId(), test.language(), s);
        }

        resultWriter.save(result);
        publisher.publishAuthorshipResultComputed(result);
    }
}
