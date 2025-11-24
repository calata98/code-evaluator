package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.EvaluationDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;


class EvaluationPersistenceMapperTest {

    @Test
    void toDocument_shouldMapAllFieldsCorrectly() {
        // given
        Instant now = Instant.parse("2024-01-01T10:15:30Z");

        Map<FeedbackType, Integer> rubric = Map.of(
                FeedbackType.STYLE, 80,
                FeedbackType.READABILITY, 60
        );

        Evaluation domain = new Evaluation(
                "eval-1",
                "sub-1",
                true,
                90,
                rubric,
                "Looks good",
                now
        );

        // when
        EvaluationDocument doc = EvaluationPersistenceMapper.toDocument(domain);

        // then
        assertEquals("eval-1", doc.getId());
        assertEquals("sub-1", doc.getSubmissionId());
        assertTrue(doc.isPassed());
        assertEquals(90, doc.getScore());
        assertEquals(rubric, doc.getRubric());
        assertEquals("Looks good", doc.getJustification());
        assertEquals(now, doc.getCreatedAt());
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectly() {
        // given
        Instant now = Instant.parse("2024-01-02T11:00:00Z");

        Map<FeedbackType, Integer> rubric = Map.of(
                FeedbackType.READABILITY, 40
        );

        EvaluationDocument doc = new EvaluationDocument(
                "eval-2",
                "sub-2",
                false,
                20,
                rubric,
                "Needs improvement",
                now
        );

        // when
        Evaluation domain = EvaluationPersistenceMapper.toDomain(doc);

        // then
        assertEquals("eval-2", domain.getId());
        assertEquals("sub-2", domain.getSubmissionId());
        assertFalse(domain.isPassed());
        assertEquals(20, domain.getScore());
        assertEquals(rubric, domain.getRubric());
        assertEquals("Needs improvement", domain.getJustification());
        assertEquals(now, domain.getCreatedAt());
    }
}
