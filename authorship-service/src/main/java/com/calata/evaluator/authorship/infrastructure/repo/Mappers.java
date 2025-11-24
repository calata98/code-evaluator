package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.domain.model.AuthorshipQuestion;
import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.authorship.domain.model.Verdict;
import com.calata.evaluator.contracts.dto.AnswerDTO;
import com.calata.evaluator.contracts.dto.QuestionDTO;
import com.calata.evaluator.contracts.events.AuthorshipAnswersProvided;
import com.calata.evaluator.contracts.events.AuthorshipEvaluationComputed;
import com.calata.evaluator.contracts.events.AuthorshipTestCreated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Mappers {
    private Mappers(){}

    // Test
    public static AuthorshipTestDocument toDocument(AuthorshipTest t) {
        var d = new AuthorshipTestDocument();
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
        return d;
    }

    public static AuthorshipTest toDomain(AuthorshipTestDocument d) {
        return new AuthorshipTest(
                d.getSubmissionId(),
                d.getUserId(),
                d.getLanguage(),
                d.getQuestions().stream().map(qd ->
                        new AuthorshipQuestion(qd.getId(), qd.getPrompt(), qd.getChoices(), qd.getCorrectIndexHint())
                ).toList(),
                d.getCreatedAt(),
                d.getExpiresAt()
        );
    }

    public static AuthorshipTestCreated toEvent(AuthorshipTest t, String code) {
        List<QuestionDTO> qs = t.questions().stream()
                .map(q -> new QuestionDTO(q.id(), q.prompt(), q.choices(), q.correctIndexHint()))
                .toList();
        return new AuthorshipTestCreated(
                t.submissionId(), t.userId(),
                t.language(), qs, t.expiresAt(), t.createdAt(), code
        );
    }

    // Result
    public static AuthorshipEvaluationDocument toDocument(AuthorshipEvaluation r) {
        var d = new AuthorshipEvaluationDocument();
        d.setSubmissionId(r.submissionId());
        d.setLanguage(r.language());
        d.setConfidence(r.confidence());
        d.setVerdict(r.verdict().name());
        d.setJustification(r.justification());
        d.setCreatedAt(r.createdAt());
        return d;
    }

    public static AuthorshipEvaluation toDomain(AuthorshipEvaluationDocument d) {
        return new AuthorshipEvaluation(
                d.getSubmissionId(),
                d.getUserId(),
                d.getLanguage(),
                d.getConfidence(),
                Verdict.valueOf(d.getVerdict()),
                d.getJustification(),
                d.getCreatedAt()
        );
    }

    public static AuthorshipEvaluationComputed toEvent(AuthorshipEvaluation r) {
        return new AuthorshipEvaluationComputed(
                r.submissionId(), r.userId(), r.language(), r.confidence(), r.verdict().name(), r.justification(), r.createdAt()
        );
    }

    // Answers
    public static AuthorshipAnswersProvided toEvent(String submissionId, String userId, Map<String, Integer> answers) {
        List<AnswerDTO> qs = answers.entrySet().stream()
                .map(e -> new AnswerDTO(e.getKey(), e.getValue().toString()))
                .toList();
        return new AuthorshipAnswersProvided(submissionId, userId, qs);
    }
}
