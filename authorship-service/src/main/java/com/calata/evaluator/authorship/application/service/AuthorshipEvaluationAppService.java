package com.calata.evaluator.authorship.application.service;

import com.calata.evaluator.authorship.application.command.ProcessAuthorshipAnswersCommand;
import com.calata.evaluator.authorship.application.port.in.GetAuthorshipEvaluationQuery;
import com.calata.evaluator.authorship.application.port.in.HandleAuthorshipAnswersUseCase;
import com.calata.evaluator.authorship.application.port.out.*;
import com.calata.evaluator.authorship.domain.model.AuthorshipAnswer;
import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.authorship.domain.service.QuizGrader;
import com.calata.evaluator.authorship.infrastructure.submission.SubmissionCodeRestAdapter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthorshipEvaluationAppService implements HandleAuthorshipAnswersUseCase, GetAuthorshipEvaluationQuery {

    private final AuthorshipTestReader testReader;
    private final AuthorshipEvaluationWriter resultWriter;
    private final AuthorshipEvaluationReader resultReader;
    private final AuthorshipEvaluationComputedPublisher publisher;
    private final AITestEvaluator evaluator;
    private final QuizGrader grader;
    private final SubmissionCodeRestAdapter submissionCodeAdapter;

    @Override
    public void handle(ProcessAuthorshipAnswersCommand cmd) {
        AuthorshipTest test = testReader.findByTestId(cmd.submissionId()).blockOptional()
                .orElseThrow(() -> new IllegalStateException("AuthorshipTest not found for submission " + cmd.submissionId()));

        List<AuthorshipAnswer> answers = cmd.answers().stream()
                .map(a -> new AuthorshipAnswer(a.questionId(), a.text()))
                .toList();

        String code = submissionCodeAdapter.loadById(cmd.submissionId()).code();

        AuthorshipEvaluation result = evaluator.evaluate(test, answers, code);
        if (result == null) {
            double s = grader.heuristicScore(test, answers);
            result = AuthorshipEvaluation.heuristic(cmd.submissionId(), cmd.userId(), test.language(), s);
        }

        resultWriter.save(result);
        publisher.publishAuthorshipEvaluationComputed(result);
    }

    public Mono<AuthorshipEvaluation> findBySubmissionId(String resultId) {
        return resultReader.findBySubmissionId(resultId);
    }
}
