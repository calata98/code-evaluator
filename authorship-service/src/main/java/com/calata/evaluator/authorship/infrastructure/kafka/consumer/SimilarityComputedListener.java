package com.calata.evaluator.authorship.infrastructure.kafka.consumer;

import com.calata.evaluator.authorship.application.command.ProcessSimilarityComputedCommand;
import com.calata.evaluator.authorship.application.port.in.HandleSimilarityComputedUseCase;
import com.calata.evaluator.contracts.events.SimilarityComputed;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimilarityComputedListener {

    private final HandleSimilarityComputedUseCase useCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(SimilarityComputedListener.class);

    public SimilarityComputedListener(HandleSimilarityComputedUseCase useCase, KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.useCase = useCase;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.similarityComputed}", groupId = "${app.kafka.group}")
    public void onMessage(SimilarityComputed evt) {
        var cmd = new ProcessSimilarityComputedCommand(
                evt.submissionId(), evt.userId(), evt.language(), evt.type().name(), evt.score(),
                evt.matchedSubmissionId(), evt.createdAt(), evt.code()
        );
        try {
            useCase.handle(cmd);
        } catch (Exception e) {
            logger.error("Error processing SimilarityComputed: {}", e.getMessage(), e);
            publishStepFailed(evt.submissionId(), e.getMessage());
        }
    }

    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.SIMILARITY_COMPUTED;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
