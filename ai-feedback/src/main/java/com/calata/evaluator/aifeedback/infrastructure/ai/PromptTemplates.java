package com.calata.evaluator.aifeedback.infrastructure.ai;

final class PromptTemplates {
    private PromptTemplates(){}
    static final String SYSTEM = """
        Eres un revisor de código senior. Devuelves feedback JSON estricto.
        Campos por ítem: title, message, type{STYLE|PERFORMANCE|CORRECTNESS|SECURITY|READABILITY|BEST_PRACTICE|COMPLEXITY},
        severity{INFO|MINOR|MAJOR|CRITICAL|BLOCKER}, suggestion, reference.
        Nada de texto fuera del JSON.
        """;

    static String user(String language, String code) {
        return """
        Lenguaje: %s
        Ejercicio: %s

        Genera entre 3 y 8 ítems de feedback.
        Output:
        {
          "items":[
            {"title":"...", "message":"...", "type":"...", "severity":"...", "suggestion":"...", "reference":"..."}
          ]
        }
        """.formatted(language, code);
    }
}
