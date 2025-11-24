package com.calata.evaluator.aifeedback.infrastructure.kafka.consumer;

import com.calata.evaluator.aifeedback.application.command.ProcessAIFeedbackRequestedCommand;
import com.calata.evaluator.aifeedback.application.port.in.HandleAIFeedbackRequestedUseCase;
import com.calata.evaluator.contracts.events.AIFeedbackRequested;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AIFeedbackRequestedListener {

    private final HandleAIFeedbackRequestedUseCase useCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(AIFeedbackRequestedListener.class);


    public AIFeedbackRequestedListener(HandleAIFeedbackRequestedUseCase useCase, KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.useCase = useCase;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.aiFeedbackRequested}", groupId = "${app.kafka.group}")
    public void onMessage(AIFeedbackRequested msg){
        try {
            useCase.handle(new ProcessAIFeedbackRequestedCommand(msg.evaluationId(), msg.submissionId(), msg.language(),
                    msg.code(), msg.stdout(), msg.stderr(), msg.timeMs(), msg.memoryMb()));
        } catch (Exception e) {
            logger.error("Error processing AIFeedbackRequested message", e);
            publishStepFailed(msg.submissionId(), e.getMessage());
        }
    }

    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.AI_FEEDBACK_REQUEST;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
