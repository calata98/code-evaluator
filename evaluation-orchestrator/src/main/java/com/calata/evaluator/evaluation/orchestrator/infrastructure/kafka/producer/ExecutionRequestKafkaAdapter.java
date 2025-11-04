package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.ExecutionRequest;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.ExecutionRequester;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.config.KafkaTopicsProps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class ExecutionRequestKafkaAdapter implements ExecutionRequester {

    private final KafkaTemplate<String, ExecutionRequest> kafkaTemplate;
    private final KafkaTopicsProps topics;

    public ExecutionRequestKafkaAdapter(KafkaTemplate<String, ExecutionRequest> kafkaTemplate,
            KafkaTopicsProps topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public void requestExecution(ExecutionRequest request) {
        kafkaTemplate.send(topics.executionRequests(), request.submissionId(), request);
    }
}
