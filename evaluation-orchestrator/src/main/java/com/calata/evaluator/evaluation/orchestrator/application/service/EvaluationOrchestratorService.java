package com.calata.evaluator.evaluation.orchestrator.application.service;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.contracts.events.ExecutionConstraints;
import com.calata.evaluator.contracts.events.ExecutionRequest;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessAIFeedbackCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessCodeSubmissionCommand;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessExecutionResultCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleAIFeedbackCreatedUseCase;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleCodeSubmissionUseCase;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleExecutionResultUseCase;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.*;
import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EvaluationOrchestratorService
        implements HandleCodeSubmissionUseCase, HandleExecutionResultUseCase, HandleAIFeedbackCreatedUseCase {

    private final SubmissionStatusUpdater submissionStatusUpdater;
    private final ExecutionRequester executionRequester;
    private final EvaluationWriter evaluationWriter;
    private final EvaluationCreatedPublisher evaluationCreatedPublisher;
    private final AIFeedbackRequestedPublisher aiFeedbackRequestedPublisher;
    private final SubmissionReader submissionReader;

    public EvaluationOrchestratorService(
            SubmissionStatusUpdater submissionStatusUpdater,
            ExecutionRequester executionRequester,
            EvaluationWriter evaluationWriter,
            EvaluationCreatedPublisher evaluationCreatedPublisher,
            AIFeedbackRequestedPublisher aiFeedbackRequestedPublisher,
            SubmissionReader submissionReader) {
        this.submissionStatusUpdater = submissionStatusUpdater;
        this.executionRequester = executionRequester;
        this.evaluationWriter = evaluationWriter;
        this.evaluationCreatedPublisher = evaluationCreatedPublisher;
        this.aiFeedbackRequestedPublisher = aiFeedbackRequestedPublisher;
        this.submissionReader = submissionReader;
    }

    @Override
    public void handle(ProcessCodeSubmissionCommand cmd) {
        submissionStatusUpdater.markRunning(cmd.submissionId());

        var constraints = new ExecutionConstraints(Duration.ofMillis(3000), 256L, 1);
        var request = new ExecutionRequest(
                cmd.submissionId(),
                cmd.language(),
                cmd.code(),
                constraints
        );
        executionRequester.requestExecution(request);
    }

    @Override
    public void handle(ProcessExecutionResultCommand cmd) {

        var evaluation = Evaluation.createForSubmission(
                cmd.submissionId(), 0, null, null);

        var saved = evaluationWriter.save(evaluation);

        var submission = submissionReader.findById(saved.getSubmissionId());

        var code = truncateIfNeeded(submission.code(), 16000);

        evaluationCreatedPublisher.publish(new EvaluationCreated(
                saved.getId(), saved.getSubmissionId(), submission.code(), submission.language(), saved.isPassed(), submission.userId() ,saved.getScore(),
                saved.getCreatedAt()
        ));

        aiFeedbackRequestedPublisher.publish(
                saved.getId(),
                saved.getSubmissionId(),
                submission.language(),
                code,
                cmd.stdout(),
                cmd.stderr(),
                cmd.timeMs(),
                cmd.memoryMb()
        );
    }

    @Override
    public void handle(ProcessAIFeedbackCreatedCommand command) {
        evaluationWriter.updateScoreAndRubricAndJustification(
                command.evaluationId(), command.score(), command.rubric(), command.justification());
    }

    private String truncateIfNeeded(String s, int max){
        return (s != null && s.length() > max) ? s.substring(0, max) : s;
    }
}
