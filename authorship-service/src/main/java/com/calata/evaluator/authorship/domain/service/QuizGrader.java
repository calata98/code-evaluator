package com.calata.evaluator.authorship.domain.service;

import com.calata.evaluator.authorship.domain.model.AuthorshipAnswer;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class QuizGrader {
    public double heuristicScore(AuthorshipTest test, java.util.List<AuthorshipAnswer> answers) {
        if (test.questions()==null || test.questions().isEmpty()) return 0.5;
        Map<String, AuthorshipAnswer> map = answers.stream()
                .collect(Collectors.toMap(AuthorshipAnswer::questionId, a -> a, (a,b)->a));
        int total = 0, correct = 0;
        for (var q : test.questions()) {
            total++;
            var a = map.get(q.id());
            if (a == null) continue;
            if (q.correctIndexHint() != null) {
                try {
                    int idx = Integer.parseInt(a.text().trim());
                    if (idx == q.correctIndexHint()) correct++;
                } catch (NumberFormatException ignored) {}
            }
        }
        return total == 0 ? 0.5 : (double) correct / total;
    }
}
