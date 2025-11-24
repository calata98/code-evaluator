package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.AIFeedbackCreated;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessAIFeedbackCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleAIFeedbackCreatedUseCase;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIFeedbackCreatedListenerTest {

    @Mock
    private HandleAIFeedbackCreatedUseCase useCase;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @Mock
    private AIFeedbackCreated msg;

    @Captor
    private ArgumentCaptor<ProcessAIFeedbackCreatedCommand> cmdCaptor;

    @Captor
    private ArgumentCaptor<StepFailedEvent> stepFailedCaptor;

    private AIFeedbackCreatedListener listener;

    @BeforeEach
    void setUp() {
        listener = new AIFeedbackCreatedListener(useCase, kafkaTemplate, kafkaTopicsProperties);
    }

    @Test
    void onMessage_whenUseCaseSucceeds_shouldCallUseCaseAndNotPublishStepFailed() {
        // given
        String evaluationId = "eval-123";
        int score = 85;
        Map<FeedbackType, Integer> rubric = Map.of();
        String justification = "well explained";

        when(msg.evaluationId()).thenReturn(evaluationId);
        when(msg.score()).thenReturn(score);
        when(msg.rubric()).thenReturn(rubric);
        when(msg.justification()).thenReturn(justification);

        // when
        listener.onMessage(msg);

        // then
        verify(useCase).handle(cmdCaptor.capture());
        verifyNoInteractions(kafkaTemplate);

        ProcessAIFeedbackCreatedCommand cmd = cmdCaptor.getValue();
        assertThat(cmd.evaluationId()).isEqualTo(evaluationId);
        assertThat(cmd.score()).isEqualTo(score);
        assertThat(cmd.rubric()).isEqualTo(rubric);
        assertThat(cmd.justification()).isEqualTo(justification);
    }

    @Test
    void onMessage_whenUseCaseThrows_shouldPublishStepFailedEvent() {
        // given
        String evaluationId = "eval-999";
        String errorMessage = "DB is down";
        String stepFailedTopic = "step-failed-topic";

        when(msg.evaluationId()).thenReturn(evaluationId);

        when(kafkaTopicsProperties.getStepFailed()).thenReturn(stepFailedTopic);

        doThrow(new RuntimeException(errorMessage))
                .when(useCase).handle(any(ProcessAIFeedbackCreatedCommand.class));

        // when
        listener.onMessage(msg);

        // then
        verify(useCase).handle(any(ProcessAIFeedbackCreatedCommand.class));

        verify(kafkaTemplate).send(eq(stepFailedTopic), stepFailedCaptor.capture());
        StepFailedEvent event = stepFailedCaptor.getValue();

        assertThat(event.submissionId()).isEqualTo(evaluationId);
        assertThat(event.stepName()).isEqualTo(StepNames.AI_FEEDBACK_CREATED.name());
        assertThat(event.errorCode())
                .isEqualTo(StepNames.getErrorCode(StepNames.AI_FEEDBACK_CREATED));
        assertThat(event.errorMessage()).isEqualTo(errorMessage);
    }
}
