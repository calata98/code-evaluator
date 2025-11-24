package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.contracts.events.FrontEvent;
import com.calata.evaluator.submission.api.infrastructure.notifier.UserEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseControllerTest {

    @Mock
    private UserEventBus bus;

    private SseController controller;

    @BeforeEach
    void setUp() {
        controller = new SseController(bus);
    }

    @Test
    void stream_shouldUseJwtSubjectAsUserIdAndReturnMappedSseEvents() {
        // given
        String userId = "user-123";

        Sinks.Many<FrontEvent> sink = Sinks.many().multicast().onBackpressureBuffer();
        when(bus.getSinkFor(userId)).thenReturn(sink);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId);

        // when
        Flux<ServerSentEvent<Object>> flux = controller.stream(jwt);

        FrontEvent frontEvent = mock(FrontEvent.class);
        when(frontEvent.type()).thenReturn("test-event");
        when(frontEvent.payload()).thenReturn("payload-data");

        // then
        StepVerifier.create(flux)
                .then(() -> sink.tryEmitNext(frontEvent))
                .assertNext(sse -> {
                    assertEquals("test-event", sse.event());
                    assertEquals(frontEvent, sse.data());
                })
                .thenCancel()
                .verify();

        verify(bus).getSinkFor(userId);
    }

    @Test
    void stream_shouldCompleteWhenSinkCompletes() {
        // given
        String userId = "user-456";
        Sinks.Many<FrontEvent> sink = Sinks.many().multicast().onBackpressureBuffer();
        when(bus.getSinkFor(userId)).thenReturn(sink);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId);

        // when
        Flux<ServerSentEvent<Object>> flux = controller.stream(jwt);

        // then
        StepVerifier.create(flux)
                .then(() -> sink.tryEmitComplete())
                .verifyComplete();

        verify(bus).getSinkFor(userId);
    }
}
