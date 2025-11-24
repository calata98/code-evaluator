package com.calata.evaluator.similarity.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.similarity.application.command.ProcessEvaluationCompletedCommand;
import com.calata.evaluator.similarity.application.port.in.HandleEvaluationCompletedUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EvaluationCompletedListener {

    private final HandleEvaluationCompletedUseCase useCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(EvaluationCompletedListener.class.getName());

    public EvaluationCompletedListener(HandleEvaluationCompletedUseCase useCase, KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.useCase = useCase;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.evaluationCreated}", groupId = "${app.kafka.group}")
    public void onMessage(EvaluationCreated evt) {
        var cmd = new ProcessEvaluationCompletedCommand(
                evt.submissionId(), evt.userId(), evt.language(), evt.code(), evt.createdAt());
        try {
            useCase.handle(cmd);
        } catch (Exception e) {
            logger.error("Error processing EvaluationCreated: {}", e.getMessage(), e);
            publishStepFailed(evt.submissionId(), e.getMessage());
        }
    }

    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.EVALUATION_CREATED;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
