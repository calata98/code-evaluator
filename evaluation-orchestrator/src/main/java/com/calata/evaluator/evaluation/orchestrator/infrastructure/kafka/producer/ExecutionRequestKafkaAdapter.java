package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.ExecutionRequest;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.ExecutionRequester;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class ExecutionRequestKafkaAdapter implements ExecutionRequester {

    private final KafkaTemplate<String, ExecutionRequest> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public ExecutionRequestKafkaAdapter(KafkaTemplate<String, ExecutionRequest> kafkaTemplate,
            KafkaTopicsProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public void requestExecution(ExecutionRequest request) {
        kafkaTemplate.send(topics.getExecutionRequests(), request.submissionId(), request);
    }
}
