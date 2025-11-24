package com.calata.evaluator.authorship.infrastructure.kafka.producer;

import com.calata.evaluator.authorship.application.port.out.AuthorshipEvaluationComputedPublisher;
import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import com.calata.evaluator.authorship.infrastructure.repo.Mappers;
import com.calata.evaluator.contracts.events.AuthorshipEvaluationComputed;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipEvaluationKafkaAdapter implements AuthorshipEvaluationComputedPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final KafkaTopicsProperties topics;

    public AuthorshipEvaluationKafkaAdapter(KafkaTemplate<String, Object> kafka, KafkaTopicsProperties topics) {
        this.kafka = kafka;
        this.topics = topics;
    }

    @Override
    public void publishAuthorshipEvaluationComputed(AuthorshipEvaluation result) {
        AuthorshipEvaluationComputed event = Mappers.toEvent(result);
        kafka.send(topics.getAuthorshipEvaluationComputed(), result.submissionId(), event);
    }
}
