package com.calata.evaluator.authorship.infrastructure.kafka.producer;

import com.calata.evaluator.authorship.application.port.out.AuthorshipTestCreatedPublisher;
import com.calata.evaluator.authorship.domain.model.AuthorshipTest;
import com.calata.evaluator.authorship.infrastructure.config.KafkaTopicsProps;
import com.calata.evaluator.authorship.infrastructure.repo.MongoMappers;
import com.calata.evaluator.contracts.events.AuthorshipTestCreated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipTestCreatedKafkaAdapter implements AuthorshipTestCreatedPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final KafkaTopicsProps topics;

    public AuthorshipTestCreatedKafkaAdapter(KafkaTemplate<String, Object> kafka, KafkaTopicsProps topics) {
        this.kafka = kafka; this.topics = topics;
    }

    @Override
    public void publishAuthorshipTestCreated(AuthorshipTest test) {
        AuthorshipTestCreated event = MongoMappers.toEvent(test);
        kafka.send(topics.authorshipTestCreated(), test.submissionId(), event);
    }
}
