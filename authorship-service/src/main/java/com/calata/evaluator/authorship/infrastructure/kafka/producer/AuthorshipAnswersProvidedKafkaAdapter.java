package com.calata.evaluator.authorship.infrastructure.kafka.producer;

import com.calata.evaluator.authorship.application.port.out.AuthorshipAnswersProvidedPublisher;
import com.calata.evaluator.contracts.events.AuthorshipAnswersProvided;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipAnswersProvidedKafkaAdapter implements AuthorshipAnswersProvidedPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final KafkaTopicsProperties topics;

    public AuthorshipAnswersProvidedKafkaAdapter(KafkaTemplate<String, Object> kafka, KafkaTopicsProperties topics) {
        this.kafka = kafka;
        this.topics = topics;
    }

    @Override
    public void publishAuthorshipAnswersProvided(AuthorshipAnswersProvided answersProvided) {
        kafka.send(topics.getAuthorshipAnswersProvided(), answersProvided.submissionId(), answersProvided);
    }
}
