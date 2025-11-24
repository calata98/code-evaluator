package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.ExecutionRequest;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.ExecutionRequester;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionRequestKafkaAdapterTest {

    @Mock
    private KafkaTemplate<String, ExecutionRequest> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties topics;

    @Mock
    private ExecutionRequest request;

    private ExecutionRequestKafkaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExecutionRequestKafkaAdapter(kafkaTemplate, topics);
    }

    @Test
    void requestExecution_shouldSendRequestToCorrectTopicWithSubmissionIdAsKey() {
        // given
        String submissionId = "sub-123";
        String topic = "execution-requests-topic";

        when(request.submissionId()).thenReturn(submissionId);
        when(topics.getExecutionRequests()).thenReturn(topic);

        // when
        adapter.requestExecution(request);

        // then
        verify(kafkaTemplate).send(eq(topic), eq(submissionId), eq(request));
    }
}
