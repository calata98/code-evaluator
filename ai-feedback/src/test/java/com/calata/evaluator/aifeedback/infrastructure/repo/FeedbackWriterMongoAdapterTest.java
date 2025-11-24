package com.calata.evaluator.aifeedback.infrastructure.repo;

import com.calata.evaluator.aifeedback.domain.model.Feedback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackWriterMongoAdapterTest {

    @Mock
    private SpringDataFeedbackRepository repo;

    @InjectMocks
    private FeedbackWriterMongoAdapter adapter;

    @Test
    void saveAll_shouldMapItemsToDocumentsSaveThemAndReturnOriginalItems() {
        // given
        String evaluationId = "eval-123";

        Feedback f1 = mock(Feedback.class);
        Feedback f2 = mock(Feedback.class);
        List<Feedback> items = List.of(f1, f2);

        // Mongo documents
        FeedbackDocument doc1 = mock(FeedbackDocument.class);
        FeedbackDocument doc2 = mock(FeedbackDocument.class);

        try (MockedStatic<FeedbackMongoMapper> mocked = mockStatic(FeedbackMongoMapper.class)) {
            mocked.when(() -> FeedbackMongoMapper.toDoc(eq(evaluationId), eq(f1)))
                    .thenReturn(doc1);
            mocked.when(() -> FeedbackMongoMapper.toDoc(eq(evaluationId), eq(f2)))
                    .thenReturn(doc2);

            // when
            List<Feedback> result = adapter.saveAll(evaluationId, items);

            // then
            mocked.verify(() -> FeedbackMongoMapper.toDoc(evaluationId, f1));
            mocked.verify(() -> FeedbackMongoMapper.toDoc(evaluationId, f2));

            verify(repo).saveAll(argThat(iterable -> {
                List<FeedbackDocument> docs = (List<FeedbackDocument>) iterable;
                return docs.size() == 2 && docs.contains(doc1) && docs.contains(doc2);
            }));

            assertSame(items, result);
        }
    }

    @Test
    void existsForEvaluation_shouldDelegateToRepository() {
        // given
        String evaluationId = "eval-456";
        when(repo.existsByEvaluationId(evaluationId)).thenReturn(true);

        // when
        boolean exists = adapter.existsForEvaluation(evaluationId);

        // then
        assertTrue(exists);
        verify(repo).existsByEvaluationId(evaluationId);
    }
}
