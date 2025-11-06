package com.calata.evaluator.similarity.domain.service;

import org.springframework.stereotype.Component;

@Component
public class NormalizationService {

    public String normalize(String code, String language) {
        String noComments = switch (language) {
            case "JAVA" -> code.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//.*?$", "");
            case "PYTHON" -> code.replaceAll("#.*?$", "");
            default -> code;
        };
        String noStrings = noComments.replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"S\"");
        return noStrings.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
