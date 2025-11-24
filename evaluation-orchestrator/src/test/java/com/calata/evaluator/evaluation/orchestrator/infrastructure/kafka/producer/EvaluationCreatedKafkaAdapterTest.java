package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.EvaluationCreatedPublisher;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationCreatedKafkaAdapterTest {

    @Mock
    private KafkaTemplate<String, EvaluationCreated> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties topics;

    @Mock
    private EvaluationCreated event;

    private EvaluationCreatedKafkaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EvaluationCreatedKafkaAdapter(kafkaTemplate, topics);
    }

    @Test
    void publish_shouldSendEventToCorrectTopicWithCorrectKey() {
        // given
        String evaluationId = "eval-123";
        String topic = "evaluation-created-topic";

        when(event.evaluationId()).thenReturn(evaluationId);
        when(topics.getEvaluationCreated()).thenReturn(topic);

        // when
        adapter.publish(event);

        // then
        verify(kafkaTemplate).send(eq(topic), eq(evaluationId), eq(event));
    }
}
