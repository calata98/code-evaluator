package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.submission.api.infrastructure.notifier.UserEventBus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SseController {

    private final UserEventBus bus;

    public SseController(UserEventBus bus) {
        this.bus = bus;
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> stream(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return bus.getSinkFor(userId)
                .asFlux()
                .map(e -> ServerSentEvent.builder(e.payload())
                        .event("message")
                        .build());
    }
}
