package com.calata.evaluator.authorship.infrastructure.kafka.producer;

import com.calata.evaluator.authorship.application.port.out.AuthorshipResultComputedPublisher;
import com.calata.evaluator.authorship.domain.model.AuthorshipResult;
import com.calata.evaluator.authorship.infrastructure.config.KafkaTopicsProps;
import com.calata.evaluator.authorship.infrastructure.repo.MongoMappers;
import com.calata.evaluator.contracts.events.AuthorshipResultComputed;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipResultKafkaAdapter implements AuthorshipResultComputedPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final KafkaTopicsProps topics;

    public AuthorshipResultKafkaAdapter(KafkaTemplate<String, Object> kafka, KafkaTopicsProps topics) {
        this.kafka = kafka; this.topics = topics;
    }

    @Override
    public void publishAuthorshipResultComputed(AuthorshipResult result) {
        AuthorshipResultComputed event = MongoMappers.toEvent(result);
        kafka.send(topics.authorshipResultComputed(), result.submissionId(), event);
    }
}
