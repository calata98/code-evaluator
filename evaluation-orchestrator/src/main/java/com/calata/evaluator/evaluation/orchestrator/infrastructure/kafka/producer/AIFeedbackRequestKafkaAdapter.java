package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.AIFeedbackRequested;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.AIFeedbackRequestedPublisher;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.config.KafkaTopicsProps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AIFeedbackRequestKafkaAdapter implements AIFeedbackRequestedPublisher {

    private final KafkaTemplate<String, AIFeedbackRequested> kafkaTemplate;
    private final KafkaTopicsProps topics;

    public AIFeedbackRequestKafkaAdapter(KafkaTemplate<String, AIFeedbackRequested> kafkaTemplate,
            KafkaTopicsProps topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public void publish(String evaluationId, String submissionId, String language, String code, String stdout,
            String stderr, long timeMs, long memoryMb) {
        var event = new AIFeedbackRequested(
                evaluationId, submissionId, language, code, null, null, stdout, stderr, timeMs, memoryMb
        );
        kafkaTemplate.send(topics.aiFeedbackRequested(), evaluationId, event);
    }
}
