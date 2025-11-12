package com.calata.evaluator.submission.api.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.AIFeedbackCreated;
import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.contracts.events.SubmissionCreated;
import com.calata.evaluator.contracts.events.SubmissionStatusUpdated;
import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;
import com.calata.evaluator.submission.api.domain.model.summary.EvaluationView;
import com.calata.evaluator.submission.api.domain.model.summary.FeedbackItem;
import com.calata.evaluator.submission.api.domain.model.summary.SubmissionDetailViewDocument;
import com.calata.evaluator.submission.api.domain.model.summary.SubmissionSummary;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmissionViewProjectorListener {

    private final SubmissionDetailViewRepository repo;

    @KafkaListener(topics = "submissions", groupId = "submissions-view")
    @Transactional
    public void onSubmissionCreated(SubmissionCreated evt) {
        var summary = new SubmissionSummary(
                evt.id(), evt.userId(), evt.status(), evt.title(), evt.language(),
                evt.createdAt());
        var doc = new SubmissionDetailViewDocument(
                evt.id(), summary, List.of(), Instant.now());
        repo.save(doc);
    }

    @KafkaListener(topics = "submission-status", groupId = "submissions-view")
    @Transactional
    public void onSubmissionStatusUpdated(SubmissionStatusUpdated evt) {
        repo.findById(evt.id()).ifPresent(view -> {
            var s = view.submission();
            var updatedSummary = new SubmissionSummary(
                    s.getId(), s.getUserId(), evt.status(), s.getTitle(), s.getLanguage(), s.getCreatedAt());
            repo.save(new SubmissionDetailViewDocument(
                    view.id(), updatedSummary, view.evaluations(), Instant.now()));
        });
    }

    @KafkaListener(topics = "evaluation-created", groupId = "submissions-view")
    @Transactional
    public void onEvaluationCreated(EvaluationCreated evt) {
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
        repo.save(new SubmissionDetailViewDocument(view.id(), view.submission(), evals, Instant.now()));
    }

    @KafkaListener(topics = "feedback-created", groupId = "submissions-view")
    @Transactional
    public void onAIFeedbackReady(AIFeedbackCreated evt) {
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
    }

    private SubmissionDetailViewDocument bootstrap(String submissionId) {
        var fallback = new SubmissionSummary(submissionId, null, null, null, null, Instant.EPOCH);
        var doc = new SubmissionDetailViewDocument(submissionId, fallback, List.of(), Instant.now());
        return repo.save(doc);
    }
}
