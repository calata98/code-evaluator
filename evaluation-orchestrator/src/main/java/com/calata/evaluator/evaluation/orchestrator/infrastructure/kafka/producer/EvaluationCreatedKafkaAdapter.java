package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.EvaluationCreatedPublisher;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EvaluationCreatedKafkaAdapter implements EvaluationCreatedPublisher {

    private final KafkaTemplate<String, EvaluationCreated> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public EvaluationCreatedKafkaAdapter(KafkaTemplate<String, EvaluationCreated> kafkaTemplate,
            KafkaTopicsProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public void publish(EvaluationCreated event) {
        kafkaTemplate.send(topics.getEvaluationCreated(), event.evaluationId(), event);
    }

}
