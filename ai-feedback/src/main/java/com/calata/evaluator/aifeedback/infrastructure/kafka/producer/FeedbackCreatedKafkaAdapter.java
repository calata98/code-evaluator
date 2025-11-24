package com.calata.evaluator.aifeedback.infrastructure.kafka.producer;

import com.calata.evaluator.aifeedback.application.port.out.FeedbackCreatedPublisher;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.contracts.events.AIFeedbackCreated;
import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class FeedbackCreatedKafkaAdapter implements FeedbackCreatedPublisher {

    private final KafkaTemplate<String, AIFeedbackCreated> kafka;
    private final String topic;

    public FeedbackCreatedKafkaAdapter(KafkaTemplate<String, AIFeedbackCreated> kafka, KafkaTopicsProperties props) {
        this.kafka = kafka;
        this.topic = props.getAiFeedbackCreated();
    }

    @Override
    public void publish(String evaluationId, String submissionId, List<Feedback> items, int score, Map<FeedbackType,
            Integer> rubric, String justification) {
        AIFeedbackCreated event = toContract(evaluationId, submissionId, items, score, rubric, justification);
        kafka.send(topic, evaluationId, event);
    }

    private static AIFeedbackCreated toContract(String evaluationId, String submissionId, List<Feedback> list,
            int score, Map<FeedbackType, Integer> rubric, String justification) {
        List<AIFeedbackCreated.Item> out = list.stream().map(f ->
                new AIFeedbackCreated.Item(
                        f.getId(),
                        f.getTitle(),
                        f.getMessage(),
                        f.getType().name(),
                        f.getSeverity(),
                        f.getSuggestion(),
                        f.getReference()
                )
        ).toList();
        return new AIFeedbackCreated(evaluationId, submissionId, out, Instant.now(), score, rubric, justification);
    }
}
