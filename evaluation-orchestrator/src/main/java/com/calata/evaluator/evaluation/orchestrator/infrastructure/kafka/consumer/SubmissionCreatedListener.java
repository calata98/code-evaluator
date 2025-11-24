package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.contracts.events.SubmissionCreated;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessSubmissionCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleCodeSubmissionUseCase;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SubmissionCreatedListener {

    private final HandleCodeSubmissionUseCase useCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(SubmissionCreatedListener.class);

    public SubmissionCreatedListener(HandleCodeSubmissionUseCase useCase, KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.useCase = useCase;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.submissions}", groupId = "${app.kafka.group}")
    public void onMessage(SubmissionCreated msg){
        try {
            useCase.handle(new ProcessSubmissionCreatedCommand(
                    msg.id(), msg.code(), msg.language(), msg.userId()
            ));
        } catch (Exception e) {
            logger.error("Error processing SubmissionCreated event for submissionId {}: {}", msg.id(), e.getMessage());
            publishStepFailed(msg.id(), e.getMessage());
        }
    }

    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.SUBMISSION_CREATED;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
