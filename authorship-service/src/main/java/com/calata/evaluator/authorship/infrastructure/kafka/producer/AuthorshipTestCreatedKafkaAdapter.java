package com.calata.evaluator.authorship.infrastructure.kafka.producer;

import com.calata.evaluator.authorship.application.port.out.AuthorshipTestCreatedPublisher;
import com.calata.evaluator.contracts.events.AuthorshipTestCreated;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipTestCreatedKafkaAdapter implements AuthorshipTestCreatedPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final KafkaTopicsProperties topics;

    public AuthorshipTestCreatedKafkaAdapter(KafkaTemplate<String, Object> kafka, KafkaTopicsProperties topics) {
        this.kafka = kafka;
        this.topics = topics;
    }

    @Override
    public void publishAuthorshipTestCreated(AuthorshipTestCreated event) {
        kafka.send(topics.getAuthorshipTestCreated(), event.submissionId(), event);
    }
}
