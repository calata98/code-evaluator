package com.calata.evaluator.aifeedback.infrastructure.ai;

import com.calata.evaluator.aifeedback.application.port.out.FeedbackGenerator;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.aifeedback.domain.model.FeedbackType;
import com.calata.evaluator.contracts.events.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpringAIFeedbackGeneratorAdapter implements FeedbackGenerator {

    private final ChatClient chat;
    private final ObjectMapper mapper;

    public SpringAIFeedbackGeneratorAdapter(ChatClient chatClient, ObjectMapper mapper) {
        this.chat = chatClient;
        this.mapper = mapper;
    }

    @Override
    public List<Feedback> generate(String language, String code) {
        var resp = chat.prompt()
                .system(PromptTemplates.SYSTEM)
                .user(PromptTemplates.user(language, code))
                .call()
                .content();

        try {
            JsonNode root = mapper.readTree(resp);
            JsonNode items = root.path("items");
            List<Feedback> out = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode n : items) {
                    out.add(new Feedback(
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
            return out;
        } catch (Exception e) {
            // fallback minimal si el modelo no devuelve JSON válido
            return List.of(Feedback.of(
                    "Revisión automática",
                    resp.length()>1000 ? resp.substring(0, 1000) : resp,
                    FeedbackType.READABILITY,
                    Severity.INFO,
                    "Refactoriza en funciones pequeñas.",
                    null
            ));
        }
    }

    private static FeedbackType parseType(String t){
        try { return FeedbackType.valueOf(t.toUpperCase()); } catch(Exception e){ return FeedbackType.READABILITY; }
    }
    private static Severity parseSeverity(String s){
        try { return Severity.valueOf(s.toUpperCase()); } catch(Exception e){ return Severity.MINOR; }
    }
}
