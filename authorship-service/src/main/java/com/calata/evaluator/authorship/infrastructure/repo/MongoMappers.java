package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.domain.model.AuthorshipQuestion;
import com.calata.evaluator.authorship.domain.model.AuthorshipResult;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.contracts.dto.QuestionDTO;
import com.calata.evaluator.contracts.events.AuthorshipResultComputed;
import com.calata.evaluator.contracts.events.AuthorshipTestCreated;

import java.util.List;
import java.util.stream.Collectors;

public final class MongoMappers {
    private MongoMappers(){}

    // Test
    public static AuthorshipTestDocument toDocument(AuthorshipTest t) {
        var d = new AuthorshipTestDocument();
        d.setTestId(t.testId());
        d.setSubmissionId(t.submissionId());
        d.setUserId(t.userId());
        d.setLanguage(t.language());
        d.setQuestions(t.questions().stream().map(q -> {
            var qd = new AuthorshipTestDocument.QuestionDoc();
            qd.setId(q.id()); qd.setPrompt(q.prompt()); qd.setChoices(q.choices()); qd.setCorrectIndexHint(q.correctIndexHint());
            return qd;
        }).collect(Collectors.toList()));
        d.setCreatedAt(t.createdAt());
        d.setExpiresAt(t.expiresAt());
        d.setAnswered(t.answered());
        return d;
    }

    public static AuthorshipTest fromDocument(AuthorshipTestDocument d) {
        return new AuthorshipTest(
                d.getTestId(),
                d.getSubmissionId(),
                d.getUserId(),
                d.getLanguage(),
                d.getQuestions().stream().map(qd ->
                        new AuthorshipQuestion(qd.getId(), qd.getPrompt(), qd.getChoices(), qd.getCorrectIndexHint())
                ).toList(),
                d.getCreatedAt(),
                d.getExpiresAt(),
                d.isAnswered()
        );
    }

    public static AuthorshipTestCreated toEvent(AuthorshipTest t) {
        List<QuestionDTO> qs = t.questions().stream()
                .map(q -> new QuestionDTO(q.id(), q.prompt(), q.choices(), q.correctIndexHint()))
                .toList();
        return new AuthorshipTestCreated(
                t.testId(), t.submissionId(), null,
                t.language(), qs, t.expiresAt(), t.createdAt()
        );
    }

    // Result
    public static AuthorshipResultDocument toDocument(AuthorshipResult r) {
        var d = new AuthorshipResultDocument();
        d.setSubmissionId(r.submissionId());
        d.setLanguage(r.language());
        d.setConfidence(r.confidence());
        d.setVerdict(r.verdict().name());
        d.setJustification(r.justification());
        d.setCreatedAt(r.createdAt());
        return d;
    }

    public static AuthorshipResultComputed toEvent(AuthorshipResult r) {
        return new AuthorshipResultComputed(
                r.submissionId(), null, r.language(), r.confidence(), r.verdict().name(), r.justification(), r.createdAt()
        );
    }
}
