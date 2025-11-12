package com.calata.evaluator.submission.api.infrastructure.notifier;

import com.calata.evaluator.contracts.events.FrontEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class UserEventBus {
    private final ConcurrentMap<String, Sinks.Many<FrontEvent>> sinks = new ConcurrentHashMap<>();

    public Sinks.Many<FrontEvent> getSinkFor(String userId) {
        return sinks.computeIfAbsent(userId, id ->
                Sinks.many().replay().latest()
        );
    }

    public void emitTo(String userId, FrontEvent event) {
        var sink = getSinkFor(userId);
        var result = sink.tryEmitNext(event);
    }
}
