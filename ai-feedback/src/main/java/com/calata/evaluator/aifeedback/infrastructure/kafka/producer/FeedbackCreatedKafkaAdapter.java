package com.calata.evaluator.aifeedback.infrastructure.kafka.producer;

import com.calata.evaluator.aifeedback.application.port.out.FeedbackCreatedPublisher;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.aifeedback.infrastructure.config.KafkaTopicsProps;
import com.calata.evaluator.contracts.events.FeedbackCreated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class FeedbackCreatedKafkaAdapter implements FeedbackCreatedPublisher {

    private final KafkaTemplate<String, FeedbackCreated> kafka;
    private final String topic;

    public FeedbackCreatedKafkaAdapter(KafkaTemplate<String, FeedbackCreated> kafka, KafkaTopicsProps props) {
        this.kafka = kafka;
        this.topic = props.feedbackCreated();
    }

    @Override
    public void publish(String evaluationId, String submissionId, List<Feedback> items) {
        FeedbackCreated event = toContract(evaluationId, submissionId, items);
        kafka.send(topic, evaluationId, event);
    }

    private static FeedbackCreated toContract(String evaluationId, String submissionId, List<Feedback> list) {
        List<FeedbackCreated.Item> out = list.stream().map(f ->
                new FeedbackCreated.Item(
                        f.title(),
                        f.message(),
                        f.type().name(),
                        f.severity(),
                        f.suggestion(),
                        f.reference()
                )
        ).toList();
        return new FeedbackCreated(evaluationId, submissionId, out, Instant.now());
    }
}
