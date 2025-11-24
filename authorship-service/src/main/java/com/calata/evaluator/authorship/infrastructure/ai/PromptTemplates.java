package com.calata.evaluator.authorship.infrastructure.ai;

import com.calata.evaluator.authorship.domain.model.AuthorshipAnswer;
import com.calata.evaluator.authorship.domain.model.AuthorshipQuestion;

import java.util.List;
import java.util.stream.Collectors;

public final class PromptTemplates {
    private PromptTemplates(){}

    public static final String QUIZ_SYSTEM = """
        You are a code authorship reviewer. Generate between 3 and 5 multiple-choice questions
        that verify whether the author truly understands the provided code.
        Return STRICT JSON:
        { "questions": [ { "id":"Q1", "prompt":"...", "choices":["...","...","..."], "correctIndexHint": 1 } ] }
        NO text outside the JSON.
      """;

    public static String quizUser(String language, String truncatedCode, String suspicionSummary) {
        return """
          Language: %s
          Code (truncated):
          ---
          %s
          ---
          Suspicion: %s
          Generate questions that require reasoning about flow, variables, edge cases, and complexity.
          Avoid trivial or purely theoretical questions.
        """.formatted(language, truncatedCode, suspicionSummary);
    }

    public static final String EVAL_SYSTEM = """
        You are an authorship evaluator.
        The exam is multiple-choice (MCQ): each question provides predefined choices, and the user selects one option.
        The user does NOT write free-text answers.
        Never assume the user "created" the answers; they only selected one of the available options.
    
        You will receive:
    
        the list of questions (each with multiple predefined choices),
    
        the user’s selected choice indices,
    
        optionally, the submitted code.
    
        Evaluate authorship only based on patterns, consistency, behavior across answers, and correlation with the submitted code — never based on wording, stylistic creation, or invented details.
    
        Return STRICT JSON ONLY in the following format:
      
        {
            "score": 0.0-1.0,
            "justification": "short, factual, based only on MCQ choice patterns and optionally code comparison",
            "verdict": "LIKELY_AUTHOR | UNCERTAIN | LIKELY_NOT_AUTHOR"
        }
      
        NO text outside the JSON.
        """;

    public static String evalUser(String language, String truncatedCode,
            List<AuthorshipQuestion> qs, List<AuthorshipAnswer> as) {
            String q = qs.stream()
                    .map(x -> "{id:\"%s\",prompt:\"%s\"}".formatted(x.id(), x.prompt().replace("\"","'")))
                    .collect(Collectors.joining(","));
            String a = as.stream()
                    .map(x -> "{qid:\"%s\",answer:\"%s\"}".formatted(x.questionId(), x.text().replace("\"","'")))
                    .collect(Collectors.joining(","));
            String code = truncatedCode == null ? "(not available)" :
                    (truncatedCode.length()>3000? truncatedCode.substring(0,3000)+"..." : truncatedCode);
            return """
          Language: %s
          Questions:[%s]
          UserAnswers:[%s]
          Code:%s
        """.formatted(language, q, a, code);
    }

}
