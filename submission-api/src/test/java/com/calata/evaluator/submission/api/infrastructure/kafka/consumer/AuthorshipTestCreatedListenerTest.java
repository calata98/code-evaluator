package com.calata.evaluator.submission.api.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.AuthorshipTestCreated;
import com.calata.evaluator.contracts.events.FrontEvent;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.submission.api.infrastructure.notifier.UserEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorshipTestCreatedListenerTest {

    @Mock
    private UserEventBus bus;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @Captor
    private ArgumentCaptor<FrontEvent> frontEventCaptor;

    @Captor
    private ArgumentCaptor<StepFailedEvent> stepFailedEventCaptor;

    private AuthorshipTestCreatedListener listener;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        listener = new AuthorshipTestCreatedListener(
                bus,
                webClientBuilder,
                "http://submissions-service",
                kafkaTemplate,
                kafkaTopicsProperties
        );
    }

    @Test
    void onEvent_withUserId_shouldEmitDirectlyWithoutHttpCallOrStepFailed() {
        // given
        AuthorshipTestCreated evt = mock(AuthorshipTestCreated.class);
        String userId = "user-123";
        String submissionId = "sub-123";
        Instant expiresAt = Instant.parse("2025-11-23T10:15:30Z");

        when(evt.userId()).thenReturn(userId);
        when(evt.submissionId()).thenReturn(submissionId);
        when(evt.expiresAt()).thenReturn(expiresAt);

        // when
        listener.onEvent(evt);

        // then
        verify(bus).emitTo(eq(userId), frontEventCaptor.capture());
        FrontEvent event = frontEventCaptor.getValue();

        assertThat(event.type()).isEqualTo("authorship-test-created");
        assertThat(event.payload())
                .isInstanceOf(AuthorshipTestCreatedListener.FrontAuthorshipTestCreated.class);

        var payload = (AuthorshipTestCreatedListener.FrontAuthorshipTestCreated) event.payload();
        assertThat(payload.submissionId()).isEqualTo(submissionId);
        assertThat(payload.expiresAt()).isEqualTo(expiresAt);

        verifyNoInteractions(kafkaTemplate);

        verify(webClient, never()).get();
    }

    @Test
    void onEvent_withoutUserIdAndHttpThrows_shouldPublishStepFailedAndNotEmitToUser() {
        // given
        AuthorshipTestCreated evt = mock(AuthorshipTestCreated.class);
        String submissionId = "sub-456";

        when(evt.userId()).thenReturn(null);
        when(evt.submissionId()).thenReturn(submissionId);

        when(webClient.get()).thenThrow(new RuntimeException("HTTP error"));
        when(kafkaTopicsProperties.getStepFailed()).thenReturn("step-failed-topic");

        // when
        listener.onEvent(evt);

        // then
        verify(bus, never()).emitTo(anyString(), any());

        verify(kafkaTemplate).send(eq("step-failed-topic"), stepFailedEventCaptor.capture());
        StepFailedEvent stepFailedEvent = stepFailedEventCaptor.getValue();

        assertThat(stepFailedEvent.submissionId()).isEqualTo(submissionId);
        assertThat(stepFailedEvent.stepName())
                .isEqualTo(StepNames.AUTHORSHIP_TEST_CREATED.name());
        assertThat(stepFailedEvent.errorCode())
                .isEqualTo(StepNames.getErrorCode(StepNames.AUTHORSHIP_TEST_CREATED));
        assertThat(stepFailedEvent.errorMessage())
                .contains("Failed to fetch submission details");

        verifyNoMoreInteractions(bus);
    }

    @Test
    void onEvent_withoutUserIdAndSubmissionNotFound_shouldDoNothing() {

        // given
        AuthorshipTestCreated evt = mock(AuthorshipTestCreated.class);
        String submissionId = "sub-789";

        when(evt.userId()).thenReturn(null);
        when(evt.submissionId()).thenReturn(submissionId);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec uriSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        @SuppressWarnings("rawtypes")
        Mono monoMock = mock(Mono.class);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(eq("/submissions/{id}"), eq(submissionId))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(monoMock);

        when(monoMock.block()).thenReturn(null);

        // when
        listener.onEvent(evt);

        // then
        verify(bus, never()).emitTo(anyString(), any());
        verifyNoInteractions(kafkaTemplate);
    }

}
