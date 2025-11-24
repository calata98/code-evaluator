package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.ExecutionResult;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessExecutionResultCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleExecutionResultUseCase;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionResultListenerTest {

    @Mock
    private HandleExecutionResultUseCase useCase;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @Captor
    private ArgumentCaptor<ProcessExecutionResultCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<StepFailedEvent> stepFailedEventCaptor;

    private ExecutionResultListener listener;

    @BeforeEach
    void setUp() {
        listener = new ExecutionResultListener(useCase, kafkaTemplate, kafkaTopicsProperties);
    }

    @Test
    void onMessage_shouldCallUseCaseWithMappedCommand_andNotPublishStepFailedOnSuccess() {
        // given
        ExecutionResult msg = mock(ExecutionResult.class);
        when(msg.submissionId()).thenReturn("sub-123");
        when(msg.stdout()).thenReturn("out");
        when(msg.stderr()).thenReturn("err");
        when(msg.timeMs()).thenReturn(150L);
        when(msg.memoryMb()).thenReturn(256L);

        // when
        listener.onMessage(msg);

        // then
        verify(useCase).handle(commandCaptor.capture());
        ProcessExecutionResultCommand cmd = commandCaptor.getValue();

        assertThat(cmd.submissionId()).isEqualTo("sub-123");
        assertThat(cmd.stdout()).isEqualTo("out");
        assertThat(cmd.stderr()).isEqualTo("err");
        assertThat(cmd.timeMs()).isEqualTo(150L);
        assertThat(cmd.memoryMb()).isEqualTo(256L);

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void onMessage_whenUseCaseThrows_shouldPublishStepFailedEvent() {
        // given
        ExecutionResult msg = mock(ExecutionResult.class);
        when(msg.submissionId()).thenReturn("sub-456");
        when(msg.stdout()).thenReturn("out");
        when(msg.stderr()).thenReturn("err");
        when(msg.timeMs()).thenReturn(200L);
        when(msg.memoryMb()).thenReturn(512L);

        RuntimeException ex = new RuntimeException("boom");
        doThrow(ex).when(useCase).handle(any(ProcessExecutionResultCommand.class));

        String stepFailedTopic = "step-failed-topic";
        when(kafkaTopicsProperties.getStepFailed()).thenReturn(stepFailedTopic);

        // when
        listener.onMessage(msg);

        // then
        verify(kafkaTemplate).send(eq(stepFailedTopic), stepFailedEventCaptor.capture());
        StepFailedEvent event = stepFailedEventCaptor.getValue();

        assertThat(event.submissionId()).isEqualTo("sub-456");
        assertThat(event.stepName()).isEqualTo(StepNames.EXECUTION_RESULT.name());
        assertThat(event.errorCode())
                .isEqualTo(StepNames.getErrorCode(StepNames.EXECUTION_RESULT));
        assertThat(event.errorMessage()).contains("boom");

        // Se ha intentado procesar el mensaje una vez
        verify(useCase, times(1)).handle(any(ProcessExecutionResultCommand.class));
    }
}
