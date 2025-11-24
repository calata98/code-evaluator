package com.calata.evaluator.similarity.infrastructure.kafka.producer;

import com.calata.evaluator.contracts.events.SimilarityComputed;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import com.calata.evaluator.similarity.infrastructure.repo.Mappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class SimilarityComputedKafkaAdapterTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaTopicsProperties topics;
    private SimilarityComputedKafkaAdapter adapter;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        topics = mock(KafkaTopicsProperties.class);
        adapter = new SimilarityComputedKafkaAdapter(kafkaTemplate, topics);
    }

    @Test
    void publishSimilarityComputed_sendsMappedEventToKafkaWithCorrectTopicAndKey() {
        // given
        String topicName = "similarity-computed-topic";
        String submissionId = "sub-123";

        SimilarityResult result = mock(SimilarityResult.class);
        SimilarityComputed event = mock(SimilarityComputed.class);

        when(topics.getSimilarityComputed()).thenReturn(topicName);
        when(result.submissionId()).thenReturn(submissionId);

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.toEvent(result)).thenReturn(event);

            // when
            adapter.publishSimilarityComputed(result);

            // then
            verify(kafkaTemplate).send(topicName, submissionId, event);
        }
    }
}
