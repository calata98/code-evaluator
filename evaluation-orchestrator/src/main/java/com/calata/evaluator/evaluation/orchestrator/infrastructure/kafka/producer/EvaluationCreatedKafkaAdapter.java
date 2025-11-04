package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.EvaluationCreatedPublisher;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.config.KafkaTopicsProps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EvaluationCreatedKafkaAdapter implements EvaluationCreatedPublisher {

    private final KafkaTemplate<String, EvaluationCreated> kafkaTemplate;
    private final KafkaTopicsProps topics;

    public EvaluationCreatedKafkaAdapter(KafkaTemplate<String, EvaluationCreated> kafkaTemplate,
            KafkaTopicsProps topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public void publish(EvaluationCreated event) {
        kafkaTemplate.send(topics.evaluationCreated(), event.evaluationId(), event);
    }

}
