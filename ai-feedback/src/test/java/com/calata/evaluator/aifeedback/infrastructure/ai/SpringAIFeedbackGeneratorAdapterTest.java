package com.calata.evaluator.aifeedback.infrastructure.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.aifeedback.domain.model.FeedbackAggregate;
import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.contracts.types.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAIFeedbackGeneratorAdapterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chat;

    @Test
    void generateWithScore_shouldParseValidJsonWithOverallAndRubric() {
        // given
        ObjectMapper mapper = new ObjectMapper();
        SpringAIFeedbackGeneratorAdapter adapter = new SpringAIFeedbackGeneratorAdapter(chat, mapper);

        String json = """
            {
              "items": [
                {
                  "title": "First",
                  "message": "First message",
                  "type": "STYLE",
                  "severity": "MAJOR",
                  "suggestion": "Use better naming",
                  "reference": "REF-1"
                },
                {
                  "title": "Second",
                  "message": "Second message",
                  "type": "unknown_type",
                  "severity": "bad_severity",
                  "suggestion": null,
                  "reference": null
                }
              ],
              "overall": {
                "score": 120,
                "justification": "Global assessment",
                "rubric": {
                  "STYLE": "110",
                  "READABILITY": "50.7",
                  "unknown_key": "33"
                }
              }
            }
            """;

        when(chat.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn(json);

        // when
        FeedbackAggregate aggregate = adapter.generateWithScore(
                "java", "code", "", "", 10L, 32L
        );

        // then
        // Score general clamped a 100
        assertEquals(100, aggregate.score());
        assertEquals("Global assessment", aggregate.justification());

        // Items
        List<Feedback> items = aggregate.items();
        assertEquals(2, items.size());

        Feedback first = items.get(0);
        assertEquals("First", first.getTitle());
        assertEquals("First message", first.getMessage());
        assertEquals(FeedbackType.STYLE, first.getType());
        assertEquals(Severity.MAJOR, first.getSeverity());
        assertEquals("Use better naming", first.getSuggestion());
        assertEquals("REF-1", first.getReference());

        Feedback second = items.get(1);
        assertEquals(FeedbackType.READABILITY, second.getType());
        assertEquals(Severity.MINOR, second.getSeverity());
        assertEquals("Second", second.getTitle());
        assertEquals("Second message", second.getMessage());

        // Rubric
        Map<FeedbackType, Integer> rubric = aggregate.rubric();
        assertNotNull(rubric);
        assertEquals(100, rubric.get(FeedbackType.STYLE));
        assertEquals(33, rubric.get(FeedbackType.READABILITY));
    }

    @Test
    void generateWithScore_shouldUseEstimateScoreWhenOverallMissing() {
        // given
        ObjectMapper mapper = new ObjectMapper();
        SpringAIFeedbackGeneratorAdapter adapter = new SpringAIFeedbackGeneratorAdapter(chat, mapper);

        String json = """
            {
              "items": [
                {
                  "title": "Critical issue",
                  "message": "Something very bad",
                  "type": "READABILITY",
                  "severity": "CRITICAL"
                }
              ]
            }
            """;

        when(chat.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn(json);

        // when
        FeedbackAggregate aggregate = adapter.generateWithScore(
                "java", "code", "", "", 10L, 32L
        );

        // then
        assertEquals(1, aggregate.items().size());
        Feedback f = aggregate.items().get(0);
        assertEquals("Critical issue", f.getTitle());
        assertEquals(Severity.CRITICAL, f.getSeverity());

        assertEquals(70, aggregate.score());
        assertNull(aggregate.justification());
        assertTrue(aggregate.rubric().isEmpty());
    }

    @Test
    void generateWithScore_shouldReturnFallbackAggregateWhenJsonIsInvalid() throws Exception {
        // given
        String resp = "this is not a json at all";

        ObjectMapper failingMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        when(failingMapper.readTree(resp)).thenThrow(new RuntimeException("parse error"));

        SpringAIFeedbackGeneratorAdapter adapter =
                new SpringAIFeedbackGeneratorAdapter(chat, failingMapper);

        when(chat.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn(resp);

        // when
        FeedbackAggregate aggregate = adapter.generateWithScore(
                "java", "code", "", "", 10L, 32L
        );

        // then
        List<Feedback> items = aggregate.items();
        assertEquals(1, items.size());

        Feedback f = items.get(0);
        assertEquals("Automatic Feedback", f.getTitle());
        assertEquals(FeedbackType.READABILITY, f.getType());
        assertEquals(Severity.INFO, f.getSeverity());
        assertEquals("Refactor to smaller functions", f.getSuggestion());
        assertNotNull(f.getMessage());
        assertTrue(f.getMessage().contains("this is not a json at all"));

        assertEquals(98, aggregate.score());

        assertEquals("Not valid JSON", aggregate.justification());
        assertTrue(aggregate.rubric().isEmpty());
    }
}
