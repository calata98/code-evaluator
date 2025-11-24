package com.calata.evaluator.submission.api.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.AuthorshipTestCreated;
import com.calata.evaluator.contracts.events.FrontEvent;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.submission.api.infrastructure.notifier.UserEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Component
public class AuthorshipTestCreatedListener {

    private final UserEventBus bus;
    private final WebClient authorshipClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(AuthorshipTestCreatedListener.class);

    public AuthorshipTestCreatedListener(
            UserEventBus bus,
            WebClient.Builder builder,
            @Value("${services.submissions.base-url}") String submissionsBaseUrl,
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties){
        this.bus = bus;
        this.authorshipClient = builder.baseUrl(submissionsBaseUrl).build();
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.authorshipTestCreated}", groupId = "${app.kafka.groups.submissions}")
    public void onEvent(AuthorshipTestCreated evt) {
        String userId = evt.userId();
        if (userId == null) {
            record SubmissionDTO(String id, String userId) {}
            try {
                var dto = authorshipClient.get()
                        .uri("/submissions/{id}", evt.submissionId())
                        .retrieve()
                        .bodyToMono(SubmissionDTO.class)
                        .block();
                userId = dto != null ? dto.userId() : null;
            } catch (Exception e) {
                logger.error("Error fetching submission details for ID {}: {}", evt.submissionId(), e.getMessage(), e);
                publishStepFailed(evt.submissionId(), "Failed to fetch submission details: " + e.getMessage());
                return;
            }
        }
        if (userId == null) return;

        var payload = new FrontAuthorshipTestCreated(
                evt.submissionId(), evt.expiresAt()
        );
        bus.emitTo(userId, new FrontEvent("authorship-test-created", payload));
    }


    public record FrontAuthorshipTestCreated(
            String submissionId, Instant expiresAt
    ) {}


    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.AUTHORSHIP_TEST_CREATED;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
