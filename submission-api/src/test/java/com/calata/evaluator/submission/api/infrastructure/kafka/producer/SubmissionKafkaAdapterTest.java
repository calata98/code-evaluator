package com.calata.evaluator.submission.api.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.SubmissionCreated;
import com.calata.evaluator.contracts.events.SubmissionStatusUpdated;
import com.calata.evaluator.contracts.types.Language;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.submission.api.domain.model.submission.Submission;
import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionKafkaAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties props;

    @Mock
    private Submission submission;

    @Captor
    private ArgumentCaptor<SubmissionCreated> submissionCreatedCaptor;

    @Captor
    private ArgumentCaptor<SubmissionStatusUpdated> statusUpdatedCaptor;

    private SubmissionKafkaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SubmissionKafkaAdapter(kafkaTemplate, props);
    }

    @Test
    void publishSubmission_shouldSendSubmissionCreatedEventToCorrectTopic() {
        // given
        String topic = "submissions-topic";
        String submissionId = "sub-123";
        String userId = "user-123";
        String title = "My exercise";
        String code = "public class Main {}";
        Instant createdAt = Instant.parse("2025-11-23T10:15:30Z");

        when(props.getSubmissions()).thenReturn(topic);

        when(submission.getId()).thenReturn(submissionId);
        when(submission.getUserId()).thenReturn(userId);
        when(submission.getStatus()).thenReturn(SubmissionStatus.COMPLETED);
        when(submission.getTitle()).thenReturn(title);
        when(submission.getLanguage()).thenReturn(Language.JAVA);
        when(submission.getCode()).thenReturn(code);
        when(submission.getCreatedAt()).thenReturn(createdAt);

        // when
        adapter.publishSubmission(submission);

        // then
        verify(kafkaTemplate).send(eq(topic), eq(submissionId), submissionCreatedCaptor.capture());
        SubmissionCreated event = submissionCreatedCaptor.getValue();

        assertThat(event.id()).isEqualTo(submissionId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.status()).isEqualTo(SubmissionStatus.RUNNING.name());
        assertThat(event.title()).isEqualTo(title);
        assertThat(event.language()).isEqualTo(Language.JAVA.name());
        assertThat(event.code()).isEqualTo(code);
        assertThat(event.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void publishSubmissionStatusUpdated_shouldSendStatusUpdatedEventToCorrectTopic() {
        // given
        String topic = "submission-status-topic";
        String submissionId = "sub-999";

        when(props.getSubmissionStatus()).thenReturn(topic);
        when(submission.getId()).thenReturn(submissionId);
        // Ajusta el enum si es distinto en tu dominio
        when(submission.getStatus()).thenReturn(SubmissionStatus.RUNNING);

        // marcamos el tiempo antes de llamar al método para validar el timestamp aproximado
        Instant before = Instant.now();

        // when
        adapter.publishSubmissionStatusUpdated(submission);

        // then
        verify(kafkaTemplate).send(eq(topic), eq(submissionId), statusUpdatedCaptor.capture());
        SubmissionStatusUpdated event = statusUpdatedCaptor.getValue();

        // Ajusta los nombres de los getters si tu record difiere
        assertThat(event.id()).isEqualTo(submissionId);
        assertThat(event.status()).isEqualTo(SubmissionStatus.RUNNING.name());
        assertThat(event.updatedAt()).isNotNull();

        // Validación suave del timestamp (entre before y ahora)
        Instant after = Instant.now();
        assertThat(event.updatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }
}
