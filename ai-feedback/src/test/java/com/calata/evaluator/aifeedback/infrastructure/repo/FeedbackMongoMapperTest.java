package com.calata.evaluator.aifeedback.infrastructure.repo;

import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.contracts.types.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackMongoMapperTest {

    @Mock
    private Feedback feedback;

    @Test
    void toDoc_shouldMapAllFieldsCorrectly() {
        // given
        String evaluationId = "eval-123";
        String title = "Title";
        String message = "Message";
        FeedbackType type = FeedbackType.STYLE;
        Severity severity = Severity.MAJOR;
        String suggestion = "Use better naming";
        String reference = "REF-1";
        Instant createdAt = Instant.now();

        when(feedback.getTitle()).thenReturn(title);
        when(feedback.getMessage()).thenReturn(message);
        when(feedback.getType()).thenReturn(type);
        when(feedback.getSeverity()).thenReturn(severity);
        when(feedback.getSuggestion()).thenReturn(suggestion);
        when(feedback.getReference()).thenReturn(reference);
        when(feedback.getCreatedAt()).thenReturn(createdAt);

        // when
        FeedbackDocument doc = FeedbackMongoMapper.toDoc(evaluationId, feedback);

        // then
        assertNotNull(doc);
        assertNull(doc.getId(), "The id should be null for a new document");
        assertEquals(evaluationId, doc.getEvaluationId());
        assertEquals(title, doc.getTitle());
        assertEquals(message, doc.getMessage());
        assertEquals(type.name(), doc.getType());
        assertEquals(severity.name(), doc.getSeverity());
        assertEquals(suggestion, doc.getSuggestion());
        assertEquals(reference, doc.getReference());
        assertEquals(createdAt, doc.getCreatedAt());
    }

    @Test
    void toDoc_shouldThrowNullPointerExceptionWhenTypeIsNull() {
        // given
        when(feedback.getType()).thenReturn(null);

        // when / then
        assertThrows(NullPointerException.class,
                () -> FeedbackMongoMapper.toDoc("eval-1", feedback),
                "if the type is null should throw NullPointerException using .name()");
    }

    @Test
    void toDoc_shouldThrowNullPointerExceptionWhenSeverityIsNull() {
        // given
        when(feedback.getType()).thenReturn(FeedbackType.READABILITY);
        when(feedback.getSeverity()).thenReturn(null);

        // when / then
        assertThrows(NullPointerException.class,
                () -> FeedbackMongoMapper.toDoc("eval-1", feedback),
                "if the severity is null should throw NullPointerException using .name()");
    }
}
