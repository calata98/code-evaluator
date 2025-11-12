package com.calata.evaluator.submission.api.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.SubmissionCreated;
import com.calata.evaluator.contracts.events.SubmissionStatusUpdated;
import com.calata.evaluator.submission.api.application.port.out.SubmissionEventsPublisher;
import com.calata.evaluator.submission.api.domain.model.submission.Submission;
import com.calata.evaluator.submission.api.infrastructure.config.KafkaTopicsProps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SubmissionKafkaAdapter implements SubmissionEventsPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProps props;

    public SubmissionKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProps props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    @Override
    public void publishSubmission(Submission submission){
        var message = new SubmissionCreated(
                submission.getId(),
                submission.getUserId(),
                submission.getStatus().name(),
                submission.getTitle(),
                submission.getLanguage().name(),
                submission.getCode(),
                submission.getCreatedAt()
        );
        kafkaTemplate.send(props.getSubmissions(), submission.getId(), message);
    }

    @Override
    public void publishSubmissionStatusUpdated(Submission submission) {
        var event = new SubmissionStatusUpdated(
                submission.getId(),
                submission.getStatus().name(),
                Instant.now()
        );
        kafkaTemplate.send(props.getSubmissionStatus(), submission.getId(), event);
    }
}
