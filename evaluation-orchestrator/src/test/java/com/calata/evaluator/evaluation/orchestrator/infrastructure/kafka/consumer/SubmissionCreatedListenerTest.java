package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.contracts.events.SubmissionCreated;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessSubmissionCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleCodeSubmissionUseCase;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionCreatedListenerTest {

    @Mock
    private HandleCodeSubmissionUseCase useCase;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @InjectMocks
    private SubmissionCreatedListener listener;

    @Test
    void onMessage_shouldInvokeUseCaseAndNotPublishStepFailed_onSuccess() {
        // given
        SubmissionCreated msg = new SubmissionCreated(
                "sub-1",
                "user-1",
                "PENDING",
                "Title",
                "JAVA",
                "code here",
                java.time.Instant.parse("2024-01-01T10:00:00Z")
        );

        // when
        listener.onMessage(msg);

        // then
        ArgumentCaptor<ProcessSubmissionCreatedCommand> cmdCaptor =
                ArgumentCaptor.forClass(ProcessSubmissionCreatedCommand.class);

        verify(useCase).handle(cmdCaptor.capture());
        verifyNoInteractions(kafkaTemplate);

        ProcessSubmissionCreatedCommand cmd = cmdCaptor.getValue();
        assertEquals("sub-1", cmd.submissionId());
        assertEquals("code here", cmd.code());
        assertEquals("JAVA", cmd.language());
        assertEquals("user-1", cmd.userId());
    }

    @Test
    void onMessage_whenUseCaseThrows_shouldPublishStepFailedEvent() {
        // given
        SubmissionCreated msg = new SubmissionCreated(
                "sub-err",
                "user-1",
                "PENDING",
                "Title",
                "JAVA",
                "code",
                java.time.Instant.parse("2024-01-01T10:00:00Z")
        );

        RuntimeException ex = new RuntimeException("boom");
        doThrow(ex).when(useCase).handle(any(ProcessSubmissionCreatedCommand.class));

        String stepFailedTopic = "step-failed-topic";
        when(kafkaTopicsProperties.getStepFailed()).thenReturn(stepFailedTopic);

        // when
        listener.onMessage(msg);

        // then
        ArgumentCaptor<StepFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(StepFailedEvent.class);

        verify(kafkaTemplate).send(eq(stepFailedTopic), eventCaptor.capture());

        StepFailedEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertEquals("sub-err", event.submissionId());
        assertEquals(StepNames.SUBMISSION_CREATED.name(), event.stepName());
        assertEquals(StepNames.getErrorCode(StepNames.SUBMISSION_CREATED), event.errorCode());
        assertEquals("boom", event.errorMessage());
    }
}
