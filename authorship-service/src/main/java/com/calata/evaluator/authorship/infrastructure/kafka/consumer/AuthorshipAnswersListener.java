package com.calata.evaluator.authorship.infrastructure.kafka.consumer;

import com.calata.evaluator.authorship.application.command.ProcessAuthorshipAnswersCommand;
import com.calata.evaluator.authorship.application.port.in.HandleAuthorshipAnswersUseCase;
import com.calata.evaluator.contracts.events.AuthorshipAnswersProvided;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipAnswersListener {

    private final HandleAuthorshipAnswersUseCase useCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(AuthorshipAnswersListener.class);

    public AuthorshipAnswersListener(HandleAuthorshipAnswersUseCase useCase, KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.useCase = useCase;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.authorshipAnswersProvided}", groupId = "${app.kafka.group}")
    public void onMessage(AuthorshipAnswersProvided evt) {
        var cmd = new ProcessAuthorshipAnswersCommand(evt.submissionId(), evt.userId(), evt.answers());
        try {
            useCase.handle(cmd);
        } catch (Exception e) {
            logger.error("Error processing AuthorshipAnswersProvided: {}", e.getMessage(), e);
            publishStepFailed(evt.submissionId(), e.getMessage());
        }
    }

    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.AUTHORSHIP_ANSWERS_PROVIDED;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
