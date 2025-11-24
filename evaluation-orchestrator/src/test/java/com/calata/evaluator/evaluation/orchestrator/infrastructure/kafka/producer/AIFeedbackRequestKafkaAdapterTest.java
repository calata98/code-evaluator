package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.AIFeedbackRequested;
import com.calata.evaluator.contracts.types.Language;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.AIFeedbackRequestedPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIFeedbackRequestKafkaAdapterTest {

    @Mock
    private KafkaTemplate<String, AIFeedbackRequested> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties topics;

    @Captor
    private ArgumentCaptor<AIFeedbackRequested> eventCaptor;

    private AIFeedbackRequestedPublisher adapter;

    @BeforeEach
    void setUp() {
        adapter = new AIFeedbackRequestKafkaAdapter(kafkaTemplate, topics);
    }

    @Test
    void publish_shouldBuildEventWithCorrectFields_andSendToKafka() {
        // given
        String evaluationId = "eval-1";
        String submissionId = "sub-1";
        Language language = Language.JAVA;
        String code = "System.out.println(\"hi\");";
        String stdout = "hi";
        String stderr = "";
        long timeMs = 123;
        long memoryMb = 56;

        String topicName = "ai-feedback-requested-topic";
        when(topics.getAiFeedbackRequested()).thenReturn(topicName);

        // when
        adapter.publish(evaluationId, submissionId, language.name(), code, stdout, stderr, timeMs, memoryMb);

        // then
        verify(kafkaTemplate).send(eq(topicName), eq(evaluationId), eventCaptor.capture());
        AIFeedbackRequested event = eventCaptor.getValue();

        assertThat(event.evaluationId()).isEqualTo(evaluationId);
        assertThat(event.submissionId()).isEqualTo(submissionId);
        assertThat(event.language()).isEqualTo(language.name());
        assertThat(event.code()).isEqualTo(code);


        assertThat(event.stdout()).isEqualTo(stdout);
        assertThat(event.stderr()).isEqualTo(stderr);
        assertThat(event.timeMs()).isEqualTo(timeMs);
        assertThat(event.memoryMb()).isEqualTo(memoryMb);
    }
}
