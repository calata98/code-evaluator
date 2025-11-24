package com.calata.evaluator.aifeedback.domain.service;

import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.contracts.types.Severity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackSynthesisServiceTest {
    private final FeedbackSynthesisService service = new FeedbackSynthesisService();

    @Test
    void validateAndNormalize_shouldSetMinorSeverityWhenNull() {
        // given
        Instant now = Instant.now();
        Feedback withoutSeverity = new Feedback(
                "Null severity",
                "Message",
                null,          // type
                null,          // severity null
                "Suggestion",
                "Ref-1",
                now
        );

        // when
        List<Feedback> result = service.validateAndNormalize(List.of(withoutSeverity));

        // then
        assertEquals(1, result.size());
        Feedback normalized = result.get(0);

        assertEquals(Severity.MINOR, normalized.getSeverity());
        assertEquals("Null severity", normalized.getTitle());
        assertEquals("Message", normalized.getMessage());
        assertEquals("Suggestion", normalized.getSuggestion());
        assertEquals("Ref-1", normalized.getReference());
        assertEquals(now, normalized.getCreatedAt());
    }

    @Test
    void validateAndNormalize_shouldKeepExistingSeverityWhenNotNull() {
        // given
        Instant now = Instant.now();
        Feedback withSeverity = new Feedback(
                "With severity",
                "Message",
                null,
                Severity.MAJOR,
                "Suggestion",
                "Ref-2",
                now
        );

        // when
        List<Feedback> result = service.validateAndNormalize(List.of(withSeverity));

        // then
        assertEquals(1, result.size());
        Feedback normalized = result.get(0);

        assertSame(withSeverity, normalized, "It should return the same instance when severity is not null");
        assertEquals(Severity.MAJOR, normalized.getSeverity());
    }

    @Test
    void validateAndNormalize_shouldReturnEmptyListWhenInputIsEmpty() {
        // when
        List<Feedback> result = service.validateAndNormalize(List.of());

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void validateAndNormalize_shouldReturnNewInstanceWhenSeverityWasNull() {
        // given
        Instant now = Instant.now();
        Feedback original = new Feedback(
                "Original",
                "Message",
                null,
                null,               // severity null
                "Suggestion",
                "Ref-4",
                now
        );

        // when
        List<Feedback> result = service.validateAndNormalize(List.of(original));

        // then
        Feedback normalized = result.get(0);
        assertNotSame(original, normalized, "When severity is null, should return a new instance");
        assertEquals(Severity.MINOR, normalized.getSeverity());
    }
}
