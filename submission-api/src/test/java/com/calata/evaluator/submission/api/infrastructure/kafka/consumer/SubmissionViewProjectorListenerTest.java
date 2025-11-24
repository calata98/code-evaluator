package com.calata.evaluator.submission.api.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.*;
import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.contracts.types.Severity;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.submission.api.application.service.SubmissionService;
import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;
import com.calata.evaluator.submission.api.domain.model.summary.EvaluationView;
import com.calata.evaluator.submission.api.domain.model.summary.FeedbackItem;
import com.calata.evaluator.submission.api.domain.model.summary.SubmissionSummary;
import com.calata.evaluator.submission.api.infrastructure.notifier.UserEventBus;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewDocument;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionViewProjectorListenerTest {

    @Mock
    private SubmissionDetailViewRepository repo;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private UserEventBus bus;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @InjectMocks
    private SubmissionViewProjectorListener listener;

    // ---------- onSubmissionCreated ----------

    @Test
    void onSubmissionCreated_shouldCreateAndSaveView() {
        // given
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        SubmissionCreated evt = new SubmissionCreated(
                "sub-1", "user-1", "PENDING", "Title", "JAVA", "code", createdAt
        );

        when(repo.save(any(SubmissionDetailViewDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        listener.onSubmissionCreated(evt);

        // then
        ArgumentCaptor<SubmissionDetailViewDocument> captor =
                ArgumentCaptor.forClass(SubmissionDetailViewDocument.class);
        verify(repo).save(captor.capture());

        SubmissionDetailViewDocument doc = captor.getValue();
        assertEquals("sub-1", doc.id());
        assertTrue(doc.evaluations().isEmpty());

        SubmissionSummary s = doc.submission();
        assertEquals("sub-1", s.getId());
        assertEquals("user-1", s.getUserId());
        assertEquals("PENDING", s.getStatus());
        assertEquals("Title", s.getTitle());
        assertEquals("JAVA", s.getLanguage());
        assertEquals(createdAt, s.getCreatedAt());
        assertFalse(s.isHasAuthorshipTest());
        assertFalse(s.isHasAuthorshipEvaluation());
        assertEquals(StepNames.SUBMISSION_CREATED.name(), s.getStepName());
    }

    @Test
    void onSubmissionCreated_whenSaveFails_shouldPublishStepFailed() {
        // given
        SubmissionCreated evt = new SubmissionCreated(
                "sub-err", "user-1", "PENDING", "Title", "JAVA", "code",
                Instant.parse("2024-01-01T10:00:00Z")
        );

        RuntimeException ex = new RuntimeException("db error");
        when(repo.save(any(SubmissionDetailViewDocument.class))).thenThrow(ex);

        String stepFailedTopic = "step-failed-topic";
        when(kafkaTopicsProperties.getStepFailed()).thenReturn(stepFailedTopic);

        // when
        listener.onSubmissionCreated(evt);

        // then
        ArgumentCaptor<StepFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(StepFailedEvent.class);
        verify(kafkaTemplate).send(eq(stepFailedTopic), eventCaptor.capture());

        StepFailedEvent event = eventCaptor.getValue();
        assertEquals("sub-err", event.submissionId());
        assertEquals(StepNames.SUBMISSION_CREATED.name(), event.stepName());
        assertEquals(StepNames.getErrorCode(StepNames.SUBMISSION_CREATED), event.errorCode());
        assertEquals("db error", event.errorMessage());
    }

    // ---------- onSubmissionStatusUpdated ----------

    @Test
    void onSubmissionStatusUpdated_shouldUpdateSummaryStatusAndSave() {
        // given
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        SubmissionSummary summary = new SubmissionSummary(
                "sub-1", "user-1", "PENDING", "Title", "JAVA",
                createdAt, false, false, null, null, null
        );
        SubmissionDetailViewDocument existing =
                new SubmissionDetailViewDocument("sub-1", summary, List.of(), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(existing));
        when(repo.save(any(SubmissionDetailViewDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SubmissionStatusUpdated evt = new SubmissionStatusUpdated(
                "sub-1", "RUNNING", Instant.parse("2024-01-01T11:00:00Z")
        );

        // when
        listener.onSubmissionStatusUpdated(evt);

        // then
        ArgumentCaptor<SubmissionDetailViewDocument> captor =
                ArgumentCaptor.forClass(SubmissionDetailViewDocument.class);
        verify(repo).save(captor.capture());

        SubmissionDetailViewDocument saved = captor.getValue();
        SubmissionSummary s = saved.submission();
        assertEquals("RUNNING", s.getStatus());
        assertEquals(StepNames.SUBMISSION_STATUS_UPDATE.name(), s.getStepName());
    }

    // ---------- onEvaluationCreated ----------

    @Test
    void onEvaluationCreated_shouldAddNewEvaluationWhenNotExisting() {
        // given
        SubmissionSummary summary = new SubmissionSummary(
                "sub-1", "user-1", "PENDING", "Title", "JAVA",
                Instant.parse("2024-01-01T10:00:00Z"), false, false, null, null, null
        );
        SubmissionDetailViewDocument view =
                new SubmissionDetailViewDocument("sub-1", summary, new ArrayList<>(), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(view));
        when(repo.save(any(SubmissionDetailViewDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Instant evalCreatedAt = Instant.parse("2024-01-01T10:05:00Z");
        EvaluationCreated evt = new EvaluationCreated(
                "eval-1", "sub-1", "code", "JAVA", true, "user-1", 0, evalCreatedAt
        );

        // when
        listener.onEvaluationCreated(evt);

        // then
        ArgumentCaptor<SubmissionDetailViewDocument> captor =
                ArgumentCaptor.forClass(SubmissionDetailViewDocument.class);
        verify(repo).save(captor.capture());

        SubmissionDetailViewDocument saved = captor.getValue();
        assertEquals(1, saved.evaluations().size());
        EvaluationView e = saved.evaluations().get(0);
        assertEquals("eval-1", e.id());
        assertEquals(0, e.score());
        assertNull(e.rubric());
        assertNull(e.justification());
        assertEquals(evalCreatedAt, e.createdAt());

        assertEquals(StepNames.EVALUATION_CREATED.name(), saved.submission().getStepName());
    }

    // ---------- onAIFeedbackReady ----------

    @Test
    void onAIFeedbackReady_shouldUpdateEvaluationAndMarkSubmissionCompleted() {
        // given
        SubmissionSummary summary = new SubmissionSummary(
                "sub-1", "user-1", "RUNNING", "Title", "JAVA",
                Instant.parse("2024-01-01T10:00:00Z"), false, false, null, null, null
        );
        EvaluationView existingEval = new EvaluationView(
                "eval-1", 0, null, null,
                Instant.parse("2024-01-01T10:05:00Z"),
                List.of()
        );
        SubmissionDetailViewDocument view =
                new SubmissionDetailViewDocument("sub-1", summary, List.of(existingEval), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(view));
        when(repo.save(any(SubmissionDetailViewDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<AIFeedbackCreated.Item> items = List.of(
                new AIFeedbackCreated.Item(
                        "f1", "Title1", "Msg1", "STYLE",
                        Severity.MAJOR, "Sug1", "Ref1"
                )
        );
        Map<FeedbackType, Integer> rubric = Map.of(FeedbackType.STYLE, 80);

        AIFeedbackCreated evt = new AIFeedbackCreated(
                "eval-1",
                "sub-1",
                items,
                Instant.parse("2024-01-01T10:06:00Z"),
                75,
                rubric,
                "Well done"
        );

        // when
        listener.onAIFeedbackReady(evt);

        // then
        ArgumentCaptor<SubmissionDetailViewDocument> captor =
                ArgumentCaptor.forClass(SubmissionDetailViewDocument.class);
        verify(repo).save(captor.capture());

        SubmissionDetailViewDocument saved = captor.getValue();
        assertEquals(1, saved.evaluations().size());
        EvaluationView ev = saved.evaluations().get(0);
        assertEquals("eval-1", ev.id());
        assertEquals(75, ev.score());
        assertEquals(rubric, ev.rubric());
        assertEquals("Well done", ev.justification());
        assertEquals(1, ev.feedbacks().size());
        FeedbackItem fi = ev.feedbacks().get(0);
        assertEquals("f1", fi.id());
        assertEquals("Title1", fi.title());
        assertEquals("Msg1", fi.message());
        assertEquals("STYLE", fi.type());
        assertEquals("Sug1", fi.suggestion());
        assertEquals(Severity.MAJOR, fi.severity());

        assertEquals(SubmissionStatus.COMPLETED.name(), saved.submission().getStatus());
    }

    @Test
    void onAIFeedbackReady_whenViewNotFound_shouldDoNothing() {
        // given
        AIFeedbackCreated evt = new AIFeedbackCreated(
                "eval-1", "sub-unknown", List.of(),
                Instant.now(), 0, Map.of(), null
        );
        when(repo.findById("sub-unknown")).thenReturn(Optional.empty());

        // when
        listener.onAIFeedbackReady(evt);

        // then
        verify(repo, never()).save(any());
    }

    // ---------- onAuthorshipTestCreated ----------

    @Test
    void onAuthorshipTestCreated_shouldMarkHasAuthorshipTestAndSave() {
        // given
        SubmissionSummary summary = new SubmissionSummary(
                "sub-1", "user-1", "PENDING", "Title", "JAVA",
                Instant.parse("2024-01-01T10:00:00Z"), false, false, null, null, null
        );
        SubmissionDetailViewDocument view =
                new SubmissionDetailViewDocument("sub-1", summary, List.of(), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(view));
        when(repo.save(any(SubmissionDetailViewDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuthorshipTestCreated evt = new AuthorshipTestCreated(
                "sub-1", "user-1", "JAVA",
                List.of(), Instant.now(), Instant.now(), "code"
        );

        // when
        listener.onAuthorshipTestCreated(evt);

        // then
        ArgumentCaptor<SubmissionDetailViewDocument> captor =
                ArgumentCaptor.forClass(SubmissionDetailViewDocument.class);
        verify(repo).save(captor.capture());

        SubmissionDetailViewDocument saved = captor.getValue();
        assertTrue(saved.submission().isHasAuthorshipTest());
    }

    // ---------- onAuthorshipAnswersProvided ----------

    @Test
    void onAuthorshipAnswersProvided_shouldMarkHasAuthorshipEvaluationSaveAndNotifyUser() {
        // given
        SubmissionSummary summary = new SubmissionSummary(
                "sub-1", "user-1", "PENDING", "Title", "JAVA",
                Instant.parse("2024-01-01T10:00:00Z"), false, false, null, null, null
        );
        SubmissionDetailViewDocument view =
                new SubmissionDetailViewDocument("sub-1", summary, List.of(), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(view));
        when(repo.save(any(SubmissionDetailViewDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuthorshipAnswersProvided evt = new AuthorshipAnswersProvided(
                "sub-1", "user-1", List.of()
        );

        // when
        listener.onAuthorshipAnswersProvided(evt);

        // then
        ArgumentCaptor<SubmissionDetailViewDocument> captor =
                ArgumentCaptor.forClass(SubmissionDetailViewDocument.class);
        verify(repo).save(captor.capture());
        SubmissionDetailViewDocument saved = captor.getValue();
        assertTrue(saved.submission().isHasAuthorshipEvaluation());

    }

    // ---------- onStepFailed ----------

    @Test
    void onStepFailed_shouldUpdateSubmissionStatusAndViewWhenPresent() {
        // given
        SubmissionSummary summary = new SubmissionSummary(
                "sub-1", "user-1", "PENDING", "Title", "JAVA",
                Instant.parse("2024-01-01T10:00:00Z"), false, false, null, null, null
        );
        SubmissionDetailViewDocument view =
                new SubmissionDetailViewDocument("sub-1", summary, List.of(), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(view));
        when(repo.save(any(SubmissionDetailViewDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StepFailedEvent evt = new StepFailedEvent(
                "sub-1", "SOME_STEP", "ERR_CODE", "Error msg"
        );

        // when
        listener.onStepFailed(evt);

        // then
        verify(submissionService)
                .updateSubmissionStatus("sub-1", SubmissionStatus.FAILED.name());

        ArgumentCaptor<SubmissionDetailViewDocument> captor =
                ArgumentCaptor.forClass(SubmissionDetailViewDocument.class);
        verify(repo).save(captor.capture());

        SubmissionDetailViewDocument saved = captor.getValue();
        assertEquals(SubmissionStatus.FAILED.name(), saved.submission().getStatus());
    }

    @Test
    void onStepFailed_whenViewNotFound_shouldOnlyUpdateSubmissionStatus() {
        // given
        StepFailedEvent evt = new StepFailedEvent(
                "sub-unknown", "SOME_STEP", "ERR_CODE", "Error msg"
        );

        when(repo.findById("sub-unknown")).thenReturn(Optional.empty());

        // when
        listener.onStepFailed(evt);

        // then
        verify(submissionService)
                .updateSubmissionStatus("sub-unknown", SubmissionStatus.FAILED.name());
        verify(repo, never()).save(any());
    }
}
