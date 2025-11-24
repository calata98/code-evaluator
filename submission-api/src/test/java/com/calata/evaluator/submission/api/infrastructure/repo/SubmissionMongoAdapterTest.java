package com.calata.evaluator.submission.api.infrastructure.repo;

import com.calata.evaluator.submission.api.domain.model.submission.Submission;
import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;
import com.calata.evaluator.submission.api.infrastructure.repo.mapper.SubmissionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionMongoAdapterTest {

    @Mock
    private SpringDataSubmissionRepository repo;

    @InjectMocks
    private SubmissionMongoAdapter adapter;

    // ---------- save(...) ----------

    @Test
    void save_whenSubmissionAlreadyExists_shouldReturnMappedExistingAndNotSaveAgain() {
        // given
        Submission input = mock(Submission.class);
        when(input.getId()).thenReturn("sub-1");

        SubmissionDocument existingDoc = mock(SubmissionDocument.class);
        when(repo.findById("sub-1")).thenReturn(Optional.of(existingDoc));

        Submission mapped = mock(Submission.class);

        try (MockedStatic<SubmissionPersistenceMapper> mocked = mockStatic(SubmissionPersistenceMapper.class)) {
            mocked.when(() -> SubmissionPersistenceMapper.toDomain(existingDoc))
                    .thenReturn(mapped);

            // when
            Submission result = adapter.save(input);

            // then
            assertSame(mapped, result);
            verify(repo).findById("sub-1");
            verify(repo, never()).save(any());
            mocked.verify(() -> SubmissionPersistenceMapper.toDomain(existingDoc));
        }
    }

    @Test
    void save_whenNotExists_shouldMapToDocumentSaveAndReturnMappedSaved() {
        // given
        Submission input = mock(Submission.class);
        when(input.getId()).thenReturn("sub-1");

        when(repo.findById("sub-1")).thenReturn(Optional.empty());

        SubmissionDocument toSave = mock(SubmissionDocument.class);
        SubmissionDocument savedDoc = mock(SubmissionDocument.class);
        Submission mapped = mock(Submission.class);

        try (MockedStatic<SubmissionPersistenceMapper> mocked = mockStatic(SubmissionPersistenceMapper.class)) {
            mocked.when(() -> SubmissionPersistenceMapper.toDocument(input))
                    .thenReturn(toSave);
            when(repo.save(toSave)).thenReturn(savedDoc);
            mocked.when(() -> SubmissionPersistenceMapper.toDomain(savedDoc))
                    .thenReturn(mapped);

            // when
            Submission result = adapter.save(input);

            // then
            assertSame(mapped, result);
            verify(repo).findById("sub-1");
            verify(repo).save(toSave);
            mocked.verify(() -> SubmissionPersistenceMapper.toDocument(input));
            mocked.verify(() -> SubmissionPersistenceMapper.toDomain(savedDoc));
        }
    }

    @Test
    void save_whenDuplicateKeyAndStillNotFoundAfterwards_shouldRethrowDuplicateKeyException() {
        // given
        Submission input = mock(Submission.class);
        when(input.getId()).thenReturn("sub-1");

        when(repo.findById("sub-1")).thenReturn(Optional.empty());

        SubmissionDocument toSave = mock(SubmissionDocument.class);
        DuplicateKeyException dupEx = new DuplicateKeyException("dup");

        try (MockedStatic<SubmissionPersistenceMapper> mocked = mockStatic(SubmissionPersistenceMapper.class)) {
            mocked.when(() -> SubmissionPersistenceMapper.toDocument(input))
                    .thenReturn(toSave);
            when(repo.save(toSave)).thenThrow(dupEx);

            // when / then
            DuplicateKeyException thrown = assertThrows(
                    DuplicateKeyException.class,
                    () -> adapter.save(input)
            );
            assertSame(dupEx, thrown);

            verify(repo, atLeastOnce()).findById("sub-1");
            verify(repo).save(toSave);
            mocked.verify(() -> SubmissionPersistenceMapper.toDocument(input));
        }
    }

    // ---------- updateStatus(...) ----------

    @Test
    void updateStatus_whenSubmissionExists_shouldUpdateStatusSaveAndReturnMapped() {
        // given
        String submissionId = "sub-1";
        String newStatus = SubmissionStatus.COMPLETED.name();

        SubmissionDocument existingDoc = mock(SubmissionDocument.class);
        SubmissionDocument updatedDoc = mock(SubmissionDocument.class);
        Submission mapped = mock(Submission.class);

        when(repo.findById(submissionId)).thenReturn(Optional.of(existingDoc));
        when(repo.save(existingDoc)).thenReturn(updatedDoc);

        try (MockedStatic<SubmissionPersistenceMapper> mocked = mockStatic(SubmissionPersistenceMapper.class)) {
            mocked.when(() -> SubmissionPersistenceMapper.toDomain(updatedDoc))
                    .thenReturn(mapped);

            // when
            Submission result = adapter.updateStatus(submissionId, newStatus);

            // then
            assertSame(mapped, result);
            verify(repo).findById(submissionId);
            verify(existingDoc).setStatus(SubmissionStatus.valueOf(newStatus));
            verify(repo).save(existingDoc);
            mocked.verify(() -> SubmissionPersistenceMapper.toDomain(updatedDoc));
        }
    }

    @Test
    void updateStatus_whenSubmissionNotFound_shouldThrowIllegalArgumentException() {
        // given
        String submissionId = "sub-404";
        when(repo.findById(submissionId)).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.updateStatus(submissionId, SubmissionStatus.COMPLETED.name())
        );

        assertTrue(ex.getMessage().contains("Submission with ID sub-404 not found."));
        verify(repo).findById(submissionId);
    }

    // ---------- getById(...) ----------

    @Test
    void getById_whenSubmissionExists_shouldReturnMappedDomain() {
        // given
        String submissionId = "sub-1";
        SubmissionDocument doc = mock(SubmissionDocument.class);
        Submission mapped = mock(Submission.class);

        when(repo.findById(submissionId)).thenReturn(Optional.of(doc));

        try (MockedStatic<SubmissionPersistenceMapper> mocked = mockStatic(SubmissionPersistenceMapper.class)) {
            mocked.when(() -> SubmissionPersistenceMapper.toDomain(doc))
                    .thenReturn(mapped);

            // when
            Submission result = adapter.getById(submissionId);

            // then
            assertSame(mapped, result);
            verify(repo).findById(submissionId);
            mocked.verify(() -> SubmissionPersistenceMapper.toDomain(doc));
        }
    }

    @Test
    void getById_whenSubmissionNotFound_shouldThrowIllegalArgumentException() {
        // given
        String submissionId = "sub-404";
        when(repo.findById(submissionId)).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.getById(submissionId)
        );

        assertTrue(ex.getMessage().contains("Submission with ID sub-404 not found."));
        verify(repo).findById(submissionId);
    }

    // ---------- getByUserId(...) ----------

    @Test
    void getByUserId_shouldMapAllDocumentsToDomain() {
        // given
        String userId = "user-1";

        SubmissionDocument doc1 = mock(SubmissionDocument.class);
        SubmissionDocument doc2 = mock(SubmissionDocument.class);

        when(repo.findByUserId(userId)).thenReturn(List.of(doc1, doc2));

        Submission sub1 = mock(Submission.class);
        Submission sub2 = mock(Submission.class);

        try (MockedStatic<SubmissionPersistenceMapper> mocked = mockStatic(SubmissionPersistenceMapper.class)) {
            mocked.when(() -> SubmissionPersistenceMapper.toDomain(doc1))
                    .thenReturn(sub1);
            mocked.when(() -> SubmissionPersistenceMapper.toDomain(doc2))
                    .thenReturn(sub2);

            // when
            List<Submission> result = adapter.getByUserId(userId);

            // then
            assertEquals(2, result.size());
            assertSame(sub1, result.get(0));
            assertSame(sub2, result.get(1));

            verify(repo).findByUserId(userId);
            mocked.verify(() -> SubmissionPersistenceMapper.toDomain(doc1));
            mocked.verify(() -> SubmissionPersistenceMapper.toDomain(doc2));
        }
    }
}
