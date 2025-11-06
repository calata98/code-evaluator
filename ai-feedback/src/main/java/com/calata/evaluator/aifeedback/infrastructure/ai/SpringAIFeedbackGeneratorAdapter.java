package com.calata.evaluator.aifeedback.infrastructure.ai;

import com.calata.evaluator.aifeedback.application.port.out.FeedbackGenerator;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.aifeedback.domain.model.FeedbackAggregate;
import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.contracts.types.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SpringAIFeedbackGeneratorAdapter implements FeedbackGenerator {

    private final ChatClient chat;
    private final ObjectMapper mapper;

    public SpringAIFeedbackGeneratorAdapter(ChatClient chatClient, ObjectMapper mapper) {
        this.chat = chatClient;
        this.mapper = mapper;
    }

    @Override
    public FeedbackAggregate generateWithScore(String language, String code, String stderr, String stdout,
            long timeMs, long memoryMb) {

        var resp = chat.prompt()
                .system(PromptTemplates.SYSTEM)
                .user(PromptTemplates.user(language, code, stderr, stdout, timeMs, memoryMb))
                .call()
                .content();

        try {
            JsonNode root = mapper.readTree(resp);

            // Feedback items
            List<Feedback> items = new ArrayList<>();
            JsonNode arr = root.path("items");
            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    items.add(new Feedback(
                            n.path("title").asText("Feedback"),
                            n.path("message").asText(),
                            parseType(n.path("type").asText()),
                            parseSeverity(n.path("severity").asText()),
                            n.path("suggestion").asText(null),
                            n.path("reference").asText(null),
                            Instant.now()
                    ));
                }
            }

            // Overall score and rubric
            JsonNode overall = root.path("overall");
            int score = clamp0to100(overall.path("score").asInt(estimateScore(items)));
            String justification = overall.path("justification").asText(null);

            Map<FeedbackType, Integer> rubric = Map.of();
            JsonNode r = overall.path("rubric");
            if (r.isObject()) {
                Map<FeedbackType, Integer> tmp = new java.util.EnumMap<>(FeedbackType.class);
                r.fields().forEachRemaining(e -> {
                    FeedbackType t = parseType(e.getKey());
                    int v = clamp0to100(asIntSafe(e.getValue().asText()));
                    tmp.put(t, v);
                });
                rubric = tmp;
            }

            return new FeedbackAggregate(score, rubric, justification, items);

        } catch (Exception e) {
            List<Feedback> items = List.of(Feedback.of(
                    "Automatic Feedback",
                    resp.length() > 1000 ? resp.substring(0, 1000) : resp,
                    FeedbackType.READABILITY,
                    Severity.INFO,
                    "Refactor to smaller functions",
                    null
            ));
            int score = estimateScore(items);
            return new FeedbackAggregate(score, Map.of(), "Not valid JSON", items);
        }
    }

    private static int clamp0to100(int v){ return Math.max(0, Math.min(100, v)); }

    private static int asIntSafe(String s){
        try { return (int)Math.round(Double.parseDouble(s)); } catch (Exception e){ return 0; }
    }

    private static FeedbackType parseType(String t){
        try { return FeedbackType.valueOf(t.toUpperCase()); } catch(Exception e){ return FeedbackType.READABILITY; }
    }
    private static Severity parseSeverity(String s){
        try { return Severity.valueOf(s.toUpperCase()); } catch(Exception e){ return Severity.MINOR; }
    }

    private static int estimateScore(List<Feedback> items){
        int penalty = 0;
        for (var f : items){
            penalty += switch (f.getSeverity()) {
                case INFO -> 2;
                case MINOR -> 5;
                case MAJOR -> 15;
                case CRITICAL -> 30;
                case BLOCKER -> 45;
            };
        }
        return clamp(penalty);
    }

    private static int clamp(int penalty){
        int score = 100 - penalty;
        if (score < 0) score = 0;
        if (score > 100) score = 100;
        return score;
    }
}
