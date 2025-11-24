package com.calata.evaluator.authorship.infrastructure.ai;

import com.calata.evaluator.authorship.application.port.out.AITestEvaluator;
import com.calata.evaluator.authorship.domain.model.AuthorshipAnswer;
import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.authorship.domain.model.Verdict;
import com.calata.evaluator.authorship.domain.service.QuizGrader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringAIAuthorQuizEvaluatorAdapter implements AITestEvaluator {

    private final ChatClient chat;
    private final QuizGrader grader;
    private final ObjectMapper mapper;

    public SpringAIAuthorQuizEvaluatorAdapter(ChatClient chat, QuizGrader grader, ObjectMapper mapper) {
        this.chat = chat;
        this.grader = grader;
        this.mapper = mapper;
    }

    @Override
    public AuthorshipEvaluation evaluate(AuthorshipTest test, List<AuthorshipAnswer> answers, String truncatedCode) {

        var content = chat.prompt()
                .system(PromptTemplates.EVAL_SYSTEM)
                .user(PromptTemplates.evalUser(test.language(), truncatedCode, test.questions(), answers))
                .call()
                .content();

        try {
            JsonNode root = mapper.readTree(content);
            var ev = mapper.treeToValue(root, EvalResponse.class);
            double score = ev.score() != null ? ev.score() : grader.heuristicScore(test, answers);
            Verdict verdict;
            try {
                verdict = Verdict.valueOf(ev.verdict());
            } catch (Exception e) {
                verdict = score >= 0.8 ? Verdict.LIKELY_AUTHOR : (score > 0.5 ? Verdict.UNCERTAIN : Verdict.LIKELY_NOT_AUTHOR);
            }

            String justification = ev.justification() != null ? ev.justification() : "Heuristic fallback.";

            return AuthorshipEvaluation.of(test.submissionId(), test.userId(), test.language(), score, verdict, justification);
        } catch (JsonProcessingException e) {
            return AuthorshipEvaluation.of(test.submissionId(), test.userId(), test.language(), 0, Verdict.UNCERTAIN, "Error parsing AI response.");
        }

    }

    private record EvalResponse(Double score, String justification, String verdict) {}
}
