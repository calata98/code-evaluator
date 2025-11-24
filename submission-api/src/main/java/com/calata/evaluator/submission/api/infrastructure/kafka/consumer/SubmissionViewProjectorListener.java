package com.calata.evaluator.submission.api.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.*;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.submission.api.application.service.SubmissionService;
import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;
import com.calata.evaluator.submission.api.domain.model.summary.EvaluationView;
import com.calata.evaluator.submission.api.domain.model.summary.FeedbackItem;
import com.calata.evaluator.submission.api.infrastructure.notifier.UserEventBus;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewDocument;
import com.calata.evaluator.submission.api.domain.model.summary.SubmissionSummary;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubmissionViewProjectorListener {

    private final SubmissionDetailViewRepository repo;
    private final SubmissionService submissionService;
    private final UserEventBus bus;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(SubmissionViewProjectorListener.class);

    public SubmissionViewProjectorListener(
            SubmissionService submissionService,
            SubmissionDetailViewRepository repo,
            UserEventBus bus,
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.repo = repo;
        this.bus = bus;
        this.submissionService = submissionService;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{kafkaTopicsProperties.submissions}", groupId = "${app.kafka.groups.submissions-view}")
    @Transactional
    public void onSubmissionCreated(SubmissionCreated evt) {
        StepNames stepName = StepNames.SUBMISSION_CREATED;
        var summary = new SubmissionSummary(
                evt.id(), evt.userId(), evt.status(), evt.title(), evt.language(),
                evt.createdAt(), false, false, stepName.name(), null, null);
        var doc = new SubmissionDetailViewDocument(
                evt.id(), summary, List.of(), Instant.now());
        try {
            repo.save(doc);
        } catch (Exception e) {
            logger.error("Error processing SubmissionCreated: {}", e.getMessage(), e);
            publishStepFailed(evt.id(), stepName.name(), StepNames.getErrorCode(stepName), e.getMessage());
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.submissionStatus}", groupId = "${app.kafka.groups.submissions-view}")
    @Transactional
    public void onSubmissionStatusUpdated(SubmissionStatusUpdated evt) {
        StepNames stepName = StepNames.SUBMISSION_STATUS_UPDATE;
        try {
            repo.findById(evt.id()).ifPresent(view -> {
                var s = view.submission();
                var updatedSummary = new SubmissionSummary(s.getId(), s.getUserId(), evt.status(), s.getTitle(),
                        s.getLanguage(), s.getCreatedAt(), s.isHasAuthorshipTest(), s.isHasAuthorshipEvaluation(), stepName.name(), s.getErrorCode(), s.getErrorMessage());
                repo.save(
                        new SubmissionDetailViewDocument(view.id(), updatedSummary, view.evaluations(), Instant.now()));
            });
        } catch (Exception e) {
            logger.error("Error processing SubmissionStatusUpdated: {}", e.getMessage(), e);
            publishStepFailed(evt.id(), stepName.name(), StepNames.getErrorCode(stepName), e.getMessage());
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.evaluationCreated}", groupId = "${app.kafka.groups.submissions-view}")
    @Transactional
    public void onEvaluationCreated(EvaluationCreated evt) {
        StepNames stepName = StepNames.EVALUATION_CREATED;
        try {
            var view = repo.findById(evt.submissionId())
                    .orElseGet(() -> bootstrap(evt.submissionId()));
            var evals = new ArrayList<>(view.evaluations());
            var existing = evals.stream().filter(e -> e.id().equals(evt.evaluationId())).findFirst();

            if (existing.isEmpty()) {
                evals.add(new EvaluationView(
                        evt.evaluationId(), 0, null, null, evt.createdAt(), List.of()));
            } else {
                var e = existing.get();
                evals.remove(e);
                evals.add(new EvaluationView(
                        e.id(), 0, null, null, evt.createdAt(), e.feedbacks()));
            }
            view.submission().setStepName(stepName.name());
            repo.save(new SubmissionDetailViewDocument(view.id(), view.submission(), evals, Instant.now()));
        } catch (Exception e) {
            logger.error("Error processing EvaluationCreated: {}", e.getMessage(), e);
            publishStepFailed(evt.submissionId(), stepName.name(), StepNames.getErrorCode(stepName), e.getMessage());
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.aiFeedbackCreated}", groupId = "${app.kafka.groups.submissions-view}")
    @Transactional
    public void onAIFeedbackReady(AIFeedbackCreated evt) {
        StepNames stepName = StepNames.AI_FEEDBACK_CREATED;
        try {
            var view = repo.findById(evt.submissionId()).orElse(null);
            if (view == null) return;

            var evals = new ArrayList<EvaluationView>();
            for (var e : view.evaluations()) {
                if (e.id().equals(evt.evaluationId())) {
                    var items = evt.items().stream().map(it ->
                            new FeedbackItem(it.id(), it.title(), it.message(), it.type(), it.suggestion(), it.severity())
                    ).toList();
                    evals.add(new EvaluationView(e.id(), evt.score(), evt.rubric(), evt.justification(), e.createdAt(), items ));
                } else {
                    evals.add(e);
                }
            }
            view.submission().setStatus(SubmissionStatus.COMPLETED.name());

            repo.save(new SubmissionDetailViewDocument(view.id(), view.submission(), evals, Instant.now()));
        } catch (Exception e) {
            logger.error("Error processing AIFeedbackCreated: {}", e.getMessage(), e);
            publishStepFailed(evt.submissionId(), stepName.name(), StepNames.getErrorCode(stepName), e.getMessage());
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.authorshipTestCreated}", groupId = "${app.kafka.groups.submissions-view}")
    @Transactional
    public void onAuthorshipTestCreated(AuthorshipTestCreated evt) {
        StepNames stepName = StepNames.AUTHORSHIP_TEST_CREATED;
        try {
            var view = repo.findById(evt.submissionId()).orElse(null);
            if (view == null) return;
            view.submission().setHasAuthorshipTest(true);
            repo.save(new SubmissionDetailViewDocument(view.id(), view.submission(), view.evaluations(), Instant.now()));
        } catch (Exception e) {
            logger.error("Error processing AuthorshipTestCreated: {}", e.getMessage(), e);
            publishStepFailed(evt.submissionId(), stepName.name(), StepNames.getErrorCode(stepName), e.getMessage());
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.authorshipAnswersProvided}", groupId = "${app.kafka.groups.submissions-view}")
    public void onAuthorshipAnswersProvided(AuthorshipAnswersProvided evt) {
        StepNames stepName = StepNames.AUTHORSHIP_ANSWERS_PROVIDED;
        try {
            var view = repo.findById(evt.submissionId()).orElse(null);
            if (view == null) return;
            view.submission().setHasAuthorshipEvaluation(true);
            repo.save(new SubmissionDetailViewDocument(view.id(), view.submission(), view.evaluations(), Instant.now()));
            bus.emitTo(evt.userId(), new FrontEvent("authorship-answers-provided", null));
        } catch (Exception e) {
            logger.error("Error processing AuthorshipAnswersProvided: {}", e.getMessage(), e);
            publishStepFailed(evt.submissionId(), stepName.name(), StepNames.getErrorCode(stepName), e.getMessage());
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.stepFailed}", groupId = "${app.kafka.groups.submissions-view}")
    public void onStepFailed(StepFailedEvent evt) {
        submissionService.updateSubmissionStatus(evt.submissionId(), SubmissionStatus.FAILED.name());
        var view = repo.findById(evt.submissionId()).orElse(null);
        if (view == null) return;
        view.submission().setStatus(SubmissionStatus.FAILED.name());
        repo.save(new SubmissionDetailViewDocument(view.id(), view.submission(), view.evaluations(), Instant.now()));
    }

    private SubmissionDetailViewDocument bootstrap(String submissionId) {
        var fallback = new SubmissionSummary(submissionId, null, null, null,
                null, Instant.EPOCH, false, false, null, null, null);
        var doc = new SubmissionDetailViewDocument(submissionId, fallback, List.of(), Instant.now());
        return repo.save(doc);
    }

    private void publishStepFailed(String submissionId, String stepName, String errorCode, String errorMessage) {
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName,
                errorCode,
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
