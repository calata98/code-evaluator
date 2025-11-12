package com.calata.evaluator.authorship.infrastructure.ai;

import com.calata.evaluator.authorship.application.port.out.AITestGenerator;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.authorship.domain.model.AuthorshipQuestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SpringAIAuthorQuizGeneratorAdapter implements AITestGenerator {

    private final ChatClient chat;
    private final ObjectMapper mapper;

    public SpringAIAuthorQuizGeneratorAdapter(ChatClient chat, ObjectMapper mapper) {
        this.chat = chat;
        this.mapper = mapper;
    }

    @Override
    public AuthorshipTest generate(String submissionId, String userId, String language, String truncatedCode, String suspicionSummary) {
        String content = chat.prompt()
                .system(PromptTemplates.QUIZ_SYSTEM)
                .user(PromptTemplates.quizUser(language, truncatedCode, suspicionSummary))
                .call()
                .content();

        try {

            // Map response
            List<AuthorshipQuestion> qs = new ArrayList<>();
            JsonNode root = mapper.readTree(content);
            JsonNode arr = root.path("questions");

            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    String id = n.path("id").asText();
                    String prompt = n.path("prompt").asText();

                    List<String> choices = new ArrayList<>();
                    JsonNode choicesNode = n.path("choices");
                    if (choicesNode.isArray()) {
                        for (JsonNode c : choicesNode) {
                            choices.add(c.asText());
                        }
                    }

                    Integer correctIndexHint = n.has("correctIndexHint") && !n.get("correctIndexHint").isNull()
                            ? n.get("correctIndexHint").asInt()
                            : null;

                    qs.add(new AuthorshipQuestion(id, prompt, choices, correctIndexHint));
                }
            }
            return new AuthorshipTest(
                    UUID.randomUUID().toString(),
                    submissionId,
                    userId,
                    language,
                    qs,
                    Instant.now(),
                    Instant.now().plus(Duration.ofHours(48)),
                    false
            );
        } catch (Exception e) {
            return new AuthorshipTest(
                    UUID.randomUUID().toString(),
                    submissionId,
                    userId,
                    language,
                    List.of(),
                    Instant.now(),
                    Instant.now().plus(Duration.ofHours(48)),
                    false
            );
        }


    }
}
