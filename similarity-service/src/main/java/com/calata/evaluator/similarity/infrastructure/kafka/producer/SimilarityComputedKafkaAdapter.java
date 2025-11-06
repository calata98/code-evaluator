package com.calata.evaluator.similarity.infrastructure.kafka.producer;

import com.calata.evaluator.similarity.application.port.out.DomainEventPublisher;
import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import com.calata.evaluator.similarity.infrastructure.config.KafkaTopicsProps;
import com.calata.evaluator.contracts.events.SimilarityComputed;
import com.calata.evaluator.similarity.infrastructure.repo.MongoMappers;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class SimilarityComputedKafkaAdapter implements DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final KafkaTopicsProps topics;

    public SimilarityComputedKafkaAdapter(KafkaTemplate<String, Object> kafka, KafkaTopicsProps topics) {
        this.kafka = kafka;
        this.topics = topics;
    }

    @Override
    public void publishSimilarityComputed(SimilarityResult result) {
        SimilarityComputed event = MongoMappers.toEvent(result);
        kafka.send(topics.similarityComputed(), result.submissionId(), event);
    }
}
