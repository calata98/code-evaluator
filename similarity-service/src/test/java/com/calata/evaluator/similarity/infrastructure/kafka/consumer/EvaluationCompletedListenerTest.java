package com.calata.evaluator.similarity.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.similarity.application.command.ProcessEvaluationCompletedCommand;
import com.calata.evaluator.similarity.application.port.in.HandleEvaluationCompletedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationCompletedListenerTest {

    @Mock
    private HandleEvaluationCompletedUseCase useCase;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @Captor
    private ArgumentCaptor<ProcessEvaluationCompletedCommand> cmdCaptor;

    @Captor
    private ArgumentCaptor<StepFailedEvent> stepFailedCaptor;

    private EvaluationCompletedListener listener;

    @BeforeEach
    void setUp() {
        listener = new EvaluationCompletedListener(useCase, kafkaTemplate, kafkaTopicsProperties);
    }

    @Test
    void onMessage_shouldCallUseCaseWithMappedCommand_andNotPublishStepFailedOnSuccess() {
        // given
        EvaluationCreated evt = mock(EvaluationCreated.class);
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");

        when(evt.submissionId()).thenReturn("sub-1");
        when(evt.userId()).thenReturn("user-1");
        when(evt.language()).thenReturn("java");
        when(evt.code()).thenReturn("System.out.println();");
        when(evt.createdAt()).thenReturn(createdAt);

        // when
        listener.onMessage(evt);

        // then
        verify(useCase).handle(cmdCaptor.capture());
        ProcessEvaluationCompletedCommand cmd = cmdCaptor.getValue();

        assertThat(cmd.submissionId()).isEqualTo("sub-1");
        assertThat(cmd.userId()).isEqualTo("user-1");
        assertThat(cmd.language()).isEqualTo("java");
        assertThat(cmd.code()).isEqualTo("System.out.println();");
        assertThat(cmd.completedAt()).isEqualTo(createdAt);

        // En el caso de éxito no se envía StepFailedEvent
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void onMessage_whenUseCaseThrows_shouldPublishStepFailedEvent() {
        // given
        EvaluationCreated evt = mock(EvaluationCreated.class);

        when(evt.submissionId()).thenReturn("sub-2");
        when(evt.userId()).thenReturn("user-2");
        when(evt.language()).thenReturn("python");
        when(evt.code()).thenReturn("print('hi')");
        when(evt.createdAt()).thenReturn(Instant.parse("2024-01-02T00:00:00Z"));

        RuntimeException ex = new RuntimeException("boom");
        doThrow(ex).when(useCase).handle(any(ProcessEvaluationCompletedCommand.class));

        String stepFailedTopic = "step-failed-topic";
        when(kafkaTopicsProperties.getStepFailed()).thenReturn(stepFailedTopic);

        // when
        listener.onMessage(evt);

        // then
        verify(kafkaTemplate).send(eq(stepFailedTopic), stepFailedCaptor.capture());
        StepFailedEvent event = stepFailedCaptor.getValue();

        assertThat(event.submissionId()).isEqualTo("sub-2");
        assertThat(event.stepName()).isEqualTo(StepNames.EVALUATION_CREATED.name());
        assertThat(event.errorCode())
                .isEqualTo(StepNames.getErrorCode(StepNames.EVALUATION_CREATED));
        assertThat(event.errorMessage()).contains("boom");

        verify(useCase).handle(any(ProcessEvaluationCompletedCommand.class));
    }
}
