package com.calata.evaluator.aifeedback.infrastructure.kafka.producer;

import static org.junit.jupiter.api.Assertions.*;

import com.calata.evaluator.aifeedback.application.command.ProcessAIFeedbackRequestedCommand;
import com.calata.evaluator.aifeedback.application.port.in.HandleAIFeedbackRequestedUseCase;
import com.calata.evaluator.aifeedback.infrastructure.kafka.consumer.AIFeedbackRequestedListener;
import com.calata.evaluator.contracts.events.AIFeedbackRequested;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIFeedbackRequestedListenerTest {

    @Mock
    private HandleAIFeedbackRequestedUseCase useCase;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @InjectMocks
    private AIFeedbackRequestedListener listener;

    @Test
    void onMessage_shouldCallUseCaseAndNotPublishStepFailed_onSuccess() {
        // given
        AIFeedbackRequested msg = mock(AIFeedbackRequested.class);

        when(msg.evaluationId()).thenReturn("eval-1");
        when(msg.submissionId()).thenReturn("sub-1");
        when(msg.language()).thenReturn("java");
        when(msg.code()).thenReturn("class X {}");
        when(msg.stdout()).thenReturn("OK");
        when(msg.stderr()).thenReturn("");
        when(msg.timeMs()).thenReturn(123L);
        when(msg.memoryMb()).thenReturn(64L);

        // when
        listener.onMessage(msg);

        // then
        ArgumentCaptor<ProcessAIFeedbackRequestedCommand> cmdCaptor =
                ArgumentCaptor.forClass(ProcessAIFeedbackRequestedCommand.class);

        verify(useCase).handle(cmdCaptor.capture());
        verifyNoInteractions(kafkaTemplate);

        ProcessAIFeedbackRequestedCommand cmd = cmdCaptor.getValue();
        assertEquals("eval-1", cmd.evaluationId());
        assertEquals("sub-1", cmd.submissionId());
        assertEquals("java", cmd.language());
        assertEquals("class X {}", cmd.code());
        assertEquals("OK", cmd.stdout());
        assertEquals("", cmd.stderr());
        assertEquals(123L, cmd.timeMs());
        assertEquals(64L, cmd.memoryMb());
    }

    @Test
    void onMessage_shouldPublishStepFailedWhenUseCaseThrows() {
        // given
        AIFeedbackRequested msg = mock(AIFeedbackRequested.class);

        when(msg.evaluationId()).thenReturn("eval-2");
        when(msg.submissionId()).thenReturn("sub-2");
        when(msg.language()).thenReturn("java");
        when(msg.code()).thenReturn("class Y {}");
        when(msg.stdout()).thenReturn("");
        when(msg.stderr()).thenReturn("Compilation error");
        when(msg.timeMs()).thenReturn(50L);
        when(msg.memoryMb()).thenReturn(32L);

        RuntimeException ex = new RuntimeException("boom");
        doThrow(ex).when(useCase).handle(any(ProcessAIFeedbackRequestedCommand.class));

        String stepFailedTopic = "step-failed-topic";
        when(kafkaTopicsProperties.getStepFailed()).thenReturn(stepFailedTopic);

        // when
        listener.onMessage(msg);

        // then
        ArgumentCaptor<StepFailedEvent> eventCaptor = ArgumentCaptor.forClass(StepFailedEvent.class);

        verify(kafkaTemplate).send(eq(stepFailedTopic), eventCaptor.capture());

        StepFailedEvent event = eventCaptor.getValue();
        assertNotNull(event);

        assertEquals("sub-2", event.submissionId());
        assertEquals(StepNames.AI_FEEDBACK_REQUEST.name(), event.stepName());

        String expectedErrorCode = StepNames.getErrorCode(StepNames.AI_FEEDBACK_REQUEST);
        assertEquals(expectedErrorCode, event.errorCode());

        assertEquals("boom", event.errorMessage());
    }
}
