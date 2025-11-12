package com.calata.evaluator.submission.api.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.dto.QuestionDTO;
import com.calata.evaluator.contracts.events.AuthorshipTestCreated;
import com.calata.evaluator.contracts.events.FrontEvent;
import com.calata.evaluator.submission.api.infrastructure.notifier.UserEventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;

@Component
public class AuthorshipTestCreatedListener {

    private final UserEventBus bus;
    private final WebClient submissionsClient;

    public AuthorshipTestCreatedListener(
            UserEventBus bus,
            WebClient.Builder builder,
            @Value("${services.submissions.base-url}") String submissionsBaseUrl) {
        this.bus = bus;
        this.submissionsClient = builder.baseUrl(submissionsBaseUrl).build();
    }

    @KafkaListener(topics = "${app.kafka.topics.authorshipTestCreated}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onEvent(AuthorshipTestCreated evt) {
        String userId = evt.userId(); // ⚠️ ideal: que venga ya en el evento
        if (userId == null) {
            record SubmissionDTO(String id, String userId) {}
            try {
                var dto = submissionsClient.get()
                        .uri("/submissions/{id}", evt.submissionId())
                        .retrieve()
                        .bodyToMono(SubmissionDTO.class)
                        .block();
                userId = dto != null ? dto.userId() : null;
            } catch (Exception ignored) {}
        }
        if (userId == null) return;

        var payload = new FrontAuthorshipTestCreated(
                evt.testId(), evt.submissionId(), evt.expiresAt()
        );
        bus.emitTo(userId, new FrontEvent("authorship-test-created", payload));
    }


    public record FrontAuthorshipTestCreated(
            String testId, String submissionId, Instant expiresAt
    ) {}
}
