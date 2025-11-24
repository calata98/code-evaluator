package com.calata.evaluator.aifeedback.infrastructure.ai;

final class PromptTemplates {
    private PromptTemplates() {}

    static final String SYSTEM = """
        You are a senior code reviewer. You return STRICT JSON, with no text outside the JSON.
        Structure:
        {
          "overall": {
            "score": 0-100,
            "rubric": {
              "READABILITY": 0-100,
              "CORRECTNESS": 0-100,
              "PERFORMANCE": 0-100,
              "SECURITY": 0-100,
              "BEST_PRACTICE": 0-100,
              "COMPLEXITY": 0-100
            },
            "justification": "brief explanation (<=80 words)"
          },
          "items":[
            {"title":"...", "message":"...", "type":"STYLE|PERFORMANCE|CORRECTNESS|SECURITY|READABILITY|BEST_PRACTICE|COMPLEXITY",
             "severity":"INFO|MINOR|MAJOR|CRITICAL|BLOCKER", "suggestion":"...", "reference":"..."}
          ]
        }
        Do not comment, explain, or include anything outside the JSON.
        """;

    static String user(String language, String code, String stderr, String stdout, long timeMs, long memoryMb) {
        return """
            Stderr: %s
            Stdout: %s
            Time (ms): %d
            Memory (MB): %d
            Language: %s
            Exercise: %s
        
            Generate between 3 and 8 feedback items and an overall evaluation.
            Scores must be integers from 0 to 100.
            """.formatted(stderr, stdout, timeMs, memoryMb, language, code);
    }
}
