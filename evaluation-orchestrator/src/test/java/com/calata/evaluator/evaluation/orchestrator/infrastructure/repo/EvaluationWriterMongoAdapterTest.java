package com.calata.evaluator.evaluation.orchestrator.infrastructure.repo;

import com.calata.evaluator.contracts.types.FeedbackType;
import com.calata.evaluator.evaluation.orchestrator.domain.model.Evaluation;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.repo.mapper.EvaluationPersistenceMapper;
import com.mongodb.DuplicateKeyException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcernResult;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationWriterMongoAdapterTest {

    @Mock
    private SpringDataEvaluationRepository repo;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private EvaluationWriterMongoAdapter adapter;

    // ---------- save(...) ----------

    @Test
    void save_whenEvaluationAlreadyExists_shouldReturnMappedExistingAndNotSaveAgain() {
        // given
        Evaluation eval = mock(Evaluation.class);
        when(eval.getSubmissionId()).thenReturn("sub-1");

        EvaluationDocument existingDoc = mock(EvaluationDocument.class);
        when(repo.findBySubmissionId("sub-1")).thenReturn(Optional.of(existingDoc));

        Evaluation mapped = mock(Evaluation.class);

        try (MockedStatic<EvaluationPersistenceMapper> mocked = mockStatic(EvaluationPersistenceMapper.class)) {
            mocked.when(() -> EvaluationPersistenceMapper.toDomain(existingDoc))
                    .thenReturn(mapped);

            // when
            Evaluation result = adapter.save(eval);

            // then
            assertSame(mapped, result);
            verify(repo).findBySubmissionId("sub-1");
            verify(repo, never()).save(any());
            mocked.verify(() -> EvaluationPersistenceMapper.toDomain(existingDoc));
        }
    }

    @Test
    void save_whenNotExists_shouldMapToDocumentSaveAndReturnMappedSaved() {
        // given
        Evaluation eval = mock(Evaluation.class);
        when(eval.getSubmissionId()).thenReturn("sub-1");

        when(repo.findBySubmissionId("sub-1")).thenReturn(Optional.empty());

        EvaluationDocument toSave = mock(EvaluationDocument.class);
        EvaluationDocument savedDoc = mock(EvaluationDocument.class);
        Evaluation mapped = mock(Evaluation.class);

        try (MockedStatic<EvaluationPersistenceMapper> mocked = mockStatic(EvaluationPersistenceMapper.class)) {
            mocked.when(() -> EvaluationPersistenceMapper.toDocument(eval))
                    .thenReturn(toSave);
            when(repo.save(toSave)).thenReturn(savedDoc);
            mocked.when(() -> EvaluationPersistenceMapper.toDomain(savedDoc))
                    .thenReturn(mapped);

            // when
            Evaluation result = adapter.save(eval);

            // then
            assertSame(mapped, result);
            verify(repo).findBySubmissionId("sub-1");
            verify(repo).save(toSave);
            mocked.verify(() -> EvaluationPersistenceMapper.toDocument(eval));
            mocked.verify(() -> EvaluationPersistenceMapper.toDomain(savedDoc));
        }
    }

    @Test
    void save_whenDuplicateKeyAndThenFound_shouldReturnMappedExisting() {
        Evaluation eval = mock(Evaluation.class);
        when(eval.getSubmissionId()).thenReturn("sub-1");

        EvaluationDocument toSave = mock(EvaluationDocument.class);
        EvaluationDocument existingDoc = mock(EvaluationDocument.class);
        Evaluation mappedExisting = mock(Evaluation.class);

        when(repo.findBySubmissionId("sub-1")).thenReturn(Optional.empty(), Optional.of(existingDoc));

        DuplicateKeyException dupEx = new DuplicateKeyException(new BsonDocument(), new ServerAddress(),
                WriteConcernResult.unacknowledged());

        try (MockedStatic<EvaluationPersistenceMapper> mocked = mockStatic(EvaluationPersistenceMapper.class)) {
            mocked.when(() -> EvaluationPersistenceMapper.toDocument(eval)).thenReturn(toSave);
            when(repo.save(toSave)).thenThrow(dupEx);
            mocked.when(() -> EvaluationPersistenceMapper.toDomain(existingDoc)).thenReturn(mappedExisting);

            Evaluation result = adapter.save(eval);

            assertSame(mappedExisting, result);
            verify(repo, atLeast(2)).findBySubmissionId("sub-1");
            verify(repo).save(toSave);
        }
    }

    @Test
    void save_whenDuplicateKeyAndStillNotFound_shouldRethrowDuplicateKeyException() {
        Evaluation eval = mock(Evaluation.class);
        when(eval.getSubmissionId()).thenReturn("sub-1");

        EvaluationDocument toSave = mock(EvaluationDocument.class);

        when(repo.findBySubmissionId("sub-1")).thenReturn(Optional.empty());

        DuplicateKeyException dupEx =
                new DuplicateKeyException(
                        new BsonDocument(),
                        new ServerAddress(),
                        WriteConcernResult.unacknowledged()
                );

        try (MockedStatic<EvaluationPersistenceMapper> mocked = mockStatic(EvaluationPersistenceMapper.class)) {
            mocked.when(() -> EvaluationPersistenceMapper.toDocument(eval)).thenReturn(toSave);
            when(repo.save(toSave)).thenThrow(dupEx);

            DuplicateKeyException thrown = assertThrows(DuplicateKeyException.class, () -> adapter.save(eval));
            assertSame(dupEx, thrown);

            verify(repo, atLeast(2)).findBySubmissionId("sub-1");
            verify(repo).save(toSave);
        }
    }

    // ---------- updateScoreAndRubricAndJustification(...) ----------

    @Test
    void updateScoreAndRubricAndJustification_shouldSetFieldsAndPassedTrueWhenScoreGreaterOrEqual60() {
        // given
        String evaluationId = "eval-1";
        int score = 80;
        Map<FeedbackType, Integer> rubric = Map.of(FeedbackType.STYLE, 90);
        String justification = "Looks good";

        // when
        adapter.updateScoreAndRubricAndJustification(evaluationId, score, rubric, justification);

        // then
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);

        verify(mongoTemplate).updateFirst(
                queryCaptor.capture(),
                updateCaptor.capture(),
                eq(EvaluationDocument.class)
        );

        Query q = queryCaptor.getValue();
        var queryObj = q.getQueryObject();
        assertEquals("eval-1", queryObj.get("_id"));

        Update u = updateCaptor.getValue();
        var updateObj = u.getUpdateObject();

        assertEquals(80, updateObj.get("$set", org.bson.Document.class).get("score"));
        assertEquals(rubric, updateObj.get("$set", org.bson.Document.class).get("rubric"));
        assertEquals("Looks good", updateObj.get("$set", org.bson.Document.class).get("justification"));
        assertEquals(true, updateObj.get("$set", org.bson.Document.class).get("passed"));
    }

    @Test
    void updateScoreAndRubricAndJustification_shouldSetPassedFalseWhenScoreLessThan60() {
        // given
        String evaluationId = "eval-2";
        int score = 40;
        Map<FeedbackType, Integer> rubric = Map.of();
        String justification = "Too many issues";

        // when
        adapter.updateScoreAndRubricAndJustification(evaluationId, score, rubric, justification);

        // then
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);

        verify(mongoTemplate).updateFirst(
                any(Query.class),
                updateCaptor.capture(),
                eq(EvaluationDocument.class)
        );

        Update u = updateCaptor.getValue();
        var updateObj = u.getUpdateObject();
        assertEquals(false, updateObj.get("$set", org.bson.Document.class).get("passed"));
    }
}
