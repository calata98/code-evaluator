package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.AIFeedbackCreated;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessAIFeedbackCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleAIFeedbackCreatedUseCase;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AIFeedbackCreatedListener {

    private final HandleAIFeedbackCreatedUseCase useCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(AIFeedbackCreatedListener.class);

    public AIFeedbackCreatedListener(HandleAIFeedbackCreatedUseCase useCase, KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.useCase = useCase;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.aiFeedbackCreated}", groupId = "${app.kafka.group}")
    public void onMessage(AIFeedbackCreated msg){
        try {
            useCase.handle(new ProcessAIFeedbackCreatedCommand(
                    msg.evaluationId(), msg.score(), msg.rubric(), msg.justification()
            ));
        } catch (Exception e) {
            logger.error("Error processing AIFeedbackCreated event for evaluationId {}: {}", msg.evaluationId(), e.getMessage());
            publishStepFailed(msg.evaluationId(), e.getMessage());
        }
    }

    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.AI_FEEDBACK_CREATED;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
