package com.calata.evaluator.evaluation.orchestrator.application.service;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.contracts.events.ExecutionConstraints;
import com.calata.evaluator.contracts.events.ExecutionRequest;
import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessAIFeedbackCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessExecutionResultCommand;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessSubmissionCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.*;
import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationOrchestratorServiceTest {

    @Mock
    private SubmissionStatusUpdater submissionStatusUpdater;

    @Mock
    private ExecutionRequester executionRequester;

    @Mock
    private EvaluationWriter evaluationWriter;

    @Mock
    private EvaluationCreatedPublisher evaluationCreatedPublisher;

    @Mock
    private AIFeedbackRequestedPublisher aiFeedbackRequestedPublisher;

    @Mock
    private SubmissionReader submissionReader;

    @InjectMocks
    private EvaluationOrchestratorService service;

    // ---------- handle(ProcessSubmissionCreatedCommand) ----------

    @Test
    void handleSubmissionCreated_shouldMarkRunningAndRequestExecutionWithExpectedConstraints() {
        // given
        ProcessSubmissionCreatedCommand cmd = mock(ProcessSubmissionCreatedCommand.class);
        when(cmd.submissionId()).thenReturn("sub-1");
        when(cmd.language()).thenReturn("JAVA");
        when(cmd.code()).thenReturn("public class Main {}");

        // when
        service.handle(cmd);

        // then
        verify(submissionStatusUpdater).markRunning("sub-1");

        ArgumentCaptor<ExecutionRequest> reqCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(executionRequester).requestExecution(reqCaptor.capture());

        ExecutionRequest req = reqCaptor.getValue();
        assertEquals("sub-1", req.submissionId());
        assertEquals("JAVA", req.language());
        assertEquals("public class Main {}", req.code());

        ExecutionConstraints c = req.constraints();
        assertEquals(Duration.ofMillis(3000), c.timeout());
        assertEquals(256L, c.memoryLimitMb());
        assertEquals(1, c.cpuShares());
    }

    // ---------- handle(ProcessExecutionResultCommand) ----------

    @Test
    void handleExecutionResult_shouldCreateSaveEvaluationPublishEvents_andNotTruncateShortCode() {
        // given
        ProcessExecutionResultCommand cmd = mock(ProcessExecutionResultCommand.class);
        when(cmd.submissionId()).thenReturn("sub-1");
        when(cmd.stdout()).thenReturn("OK");
        when(cmd.stderr()).thenReturn("");
        when(cmd.timeMs()).thenReturn(150L);
        when(cmd.memoryMb()).thenReturn(64L);

        // Evaluation.createForSubmission(...) -> evaluation
        Evaluation evaluation = mock(Evaluation.class);
        Evaluation saved = mock(Evaluation.class);

        when(saved.getId()).thenReturn("eval-1");
        when(saved.getSubmissionId()).thenReturn("sub-1");
        when(saved.isPassed()).thenReturn(true);
        when(saved.getScore()).thenReturn(90);
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        when(saved.getCreatedAt()).thenReturn(createdAt);

        SubmissionReader.SubmissionSnapshot submission = mock(SubmissionReader.SubmissionSnapshot.class);
        when(submission.code()).thenReturn("short code");
        when(submission.language()).thenReturn("JAVA");
        when(submission.userId()).thenReturn("user-1");

        when(evaluationWriter.save(evaluation)).thenReturn(saved);
        when(submissionReader.findById("sub-1")).thenReturn(submission);

        try (MockedStatic<Evaluation> mocked = mockStatic(Evaluation.class)) {
            mocked.when(() -> Evaluation.createForSubmission("sub-1", 0, null, null))
                    .thenReturn(evaluation);

            // when
            service.handle(cmd);

            // then
            mocked.verify(() -> Evaluation.createForSubmission("sub-1", 0, null, null));
            verify(evaluationWriter).save(evaluation);
            verify(submissionReader).findById("sub-1");

            ArgumentCaptor<EvaluationCreated> evalEvtCaptor =
                    ArgumentCaptor.forClass(EvaluationCreated.class);
            verify(evaluationCreatedPublisher).publish(evalEvtCaptor.capture());

            EvaluationCreated ev = evalEvtCaptor.getValue();
            assertEquals("eval-1", ev.evaluationId());
            assertEquals("sub-1", ev.submissionId());
            assertEquals("short code", ev.code());
            assertEquals("JAVA", ev.language());
            assertEquals(true, ev.passed());
            assertEquals("user-1", ev.userId());
            assertEquals(90, ev.score());
            assertEquals(createdAt, ev.createdAt());

            ArgumentCaptor<String> langCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> stdoutCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> stderrCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<Long> memCaptor = ArgumentCaptor.forClass(Long.class);

            verify(aiFeedbackRequestedPublisher).publish(
                    eq("eval-1"),
                    eq("sub-1"),
                    langCaptor.capture(),
                    codeCaptor.capture(),
                    stdoutCaptor.capture(),
                    stderrCaptor.capture(),
                    timeCaptor.capture(),
                    memCaptor.capture()
            );

            assertEquals("JAVA", langCaptor.getValue());
            assertEquals("short code", codeCaptor.getValue());
            assertEquals("OK", stdoutCaptor.getValue());
            assertEquals("", stderrCaptor.getValue());
            assertEquals(150L, timeCaptor.getValue());
            assertEquals(64L, memCaptor.getValue());
        }
    }

    @Test
    void handleExecutionResult_shouldTruncateVeryLongCodeForAIFeedback_butPublishFullCodeInEvaluationCreated() {
        // given
        ProcessExecutionResultCommand cmd = mock(ProcessExecutionResultCommand.class);
        when(cmd.submissionId()).thenReturn("sub-1");
        when(cmd.stdout()).thenReturn("OUT");
        when(cmd.stderr()).thenReturn("ERR");
        when(cmd.timeMs()).thenReturn(200L);
        when(cmd.memoryMb()).thenReturn(128L);

        Evaluation evaluation = mock(Evaluation.class);
        Evaluation saved = mock(Evaluation.class);

        when(saved.getId()).thenReturn("eval-1");
        when(saved.getSubmissionId()).thenReturn("sub-1");
        when(saved.isPassed()).thenReturn(false);
        when(saved.getScore()).thenReturn(10);
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        when(saved.getCreatedAt()).thenReturn(createdAt);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20000; i++) sb.append('X');
        String longCode = sb.toString();

        SubmissionReader.SubmissionSnapshot submission = mock(SubmissionReader.SubmissionSnapshot.class);
        when(submission.code()).thenReturn(longCode);
        when(submission.language()).thenReturn("PYTHON");
        when(submission.userId()).thenReturn("user-2");

        when(evaluationWriter.save(evaluation)).thenReturn(saved);
        when(submissionReader.findById("sub-1")).thenReturn(submission);

        try (MockedStatic<Evaluation> mocked = mockStatic(Evaluation.class)) {
            mocked.when(() -> Evaluation.createForSubmission("sub-1", 0, null, null))
                    .thenReturn(evaluation);

            // when
            service.handle(cmd);

            // then
            ArgumentCaptor<EvaluationCreated> evalEvtCaptor =
                    ArgumentCaptor.forClass(EvaluationCreated.class);
            verify(evaluationCreatedPublisher).publish(evalEvtCaptor.capture());

            EvaluationCreated ev = evalEvtCaptor.getValue();
            assertEquals(longCode.length(), ev.code().length());
            assertEquals(longCode, ev.code());

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(aiFeedbackRequestedPublisher).publish(
                    eq("eval-1"),
                    eq("sub-1"),
                    eq("PYTHON"),
                    codeCaptor.capture(),
                    eq("OUT"),
                    eq("ERR"),
                    eq(200L),
                    eq(128L)
            );

            String truncated = codeCaptor.getValue();
            assertEquals(16000, truncated.length());
            assertEquals(longCode.substring(0, 16000), truncated);
        }
    }

    // ---------- handle(ProcessAIFeedbackCreatedCommand) ----------

    @Test
    void handleAIFeedbackCreated_shouldDelegateToEvaluationWriterUpdateScoreRubricJustification() {
        // given
        ProcessAIFeedbackCreatedCommand cmd = mock(ProcessAIFeedbackCreatedCommand.class);
        when(cmd.evaluationId()).thenReturn("eval-1");
        when(cmd.score()).thenReturn(85);
        when(cmd.rubric()).thenReturn(Map.of(FeedbackType.PERFORMANCE, 90));
        when(cmd.justification()).thenReturn("Looks good");

        // when
        service.handle(cmd);

        // then
        verify(evaluationWriter).updateScoreAndRubricAndJustification(
                "eval-1",
                85,
                Map.of(FeedbackType.PERFORMANCE, 90),
                "Looks good"
        );
    }
}
