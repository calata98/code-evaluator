package com.calata.evaluator.authorship.application.service;

import com.calata.evaluator.authorship.application.command.ProcessSimilarityComputedCommand;
import com.calata.evaluator.authorship.application.port.in.HandleSimilarityComputedUseCase;
import com.calata.evaluator.authorship.application.port.out.AITestGenerator;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestCreatedPublisher;
import com.calata.evaluator.authorship.application.port.out.AuthorshipTestWriter;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthorshipTestAppService implements HandleSimilarityComputedUseCase {

    private final AITestGenerator generator;
    private final AuthorshipTestWriter testWriter;
    private final AuthorshipTestCreatedPublisher publisher;

    @Value("${authorship.suspicion.nearThreshold:0.80}")
    private double nearThreshold;

    @Value("${authorship.test.ttlHours:48}")
    private long ttlHours;

    public AuthorshipTestAppService(AITestGenerator generator,
            AuthorshipTestWriter testWriter,
            AuthorshipTestCreatedPublisher publisher) {
        this.generator = generator;
        this.testWriter = testWriter;
        this.publisher = publisher;
    }

    @Override
    public void handle(ProcessSimilarityComputedCommand cmd) {
        if (!isSuspicious(cmd.type(), cmd.score())) return;

        String codeForPrompt = cmd.code() == null ? "" : truncate(cmd.code(), 3000);
        String suspicion = cmd.type() + ":" + String.format("%.2f", cmd.score());

        AuthorshipTest test = generator.generate(
                cmd.submissionId(),
                cmd.userId(),
                cmd.language(),
                codeForPrompt,
                suspicion
        );

        var withExpiry = new AuthorshipTest(
                test.testId(),
                test.submissionId(),
                test.userId(),
                test.language(),
                test.questions(),
                test.createdAt() == null ? Instant.now() : test.createdAt(),
                test.expiresAt() == null ? Instant.now().plus(Duration.ofHours(ttlHours)) : test.expiresAt(),
                test.answered()
        );

        testWriter.save(withExpiry).block();
        publisher.publishAuthorshipTestCreated(withExpiry);
    }

    private boolean isSuspicious(String type, double score) {
        return switch (type) {
            case "EXACT", "NORMALIZED" -> true;
            case "NEAR" -> score >= nearThreshold;
            default -> false;
        };
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
