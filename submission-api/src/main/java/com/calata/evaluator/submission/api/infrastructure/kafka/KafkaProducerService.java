package com.calata.evaluator.submission.api.infrastructure.kafka;

import com.calata.evaluator.contracts.events.CodeSubmissionMessage;
import com.calata.evaluator.submission.api.application.port.out.SubmissionEventsPublisher;
import com.calata.evaluator.submission.api.domain.model.Submission;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerService implements SubmissionEventsPublisher {

    private final KafkaTemplate<String, CodeSubmissionMessage> kafkaTemplate;
    private final KafkaTopicsProps props;

    public KafkaProducerService(KafkaTemplate<String, CodeSubmissionMessage> kafkaTemplate, KafkaTopicsProps props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    @Override
    public void publishCodeSubmission(Submission submission){
        var message = new CodeSubmissionMessage(
                submission.getId(),
                submission.getUserId(),
                submission.getLanguage().name(),
                submission.getCode(),
                submission.getCreatedAt()
        );
        kafkaTemplate.send(props.getSubmissions(), submission.getId(), message);
    }
}
