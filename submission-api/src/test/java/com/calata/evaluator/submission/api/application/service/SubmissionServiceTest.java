package com.calata.evaluator.submission.api.application.service;

import com.calata.evaluator.contracts.dto.SubmissionResponse;
import com.calata.evaluator.contracts.types.Language;
import com.calata.evaluator.submission.api.application.command.CreateSubmissionCommand;
import com.calata.evaluator.submission.api.application.port.out.SubmissionEventsPublisher;
import com.calata.evaluator.submission.api.application.port.out.SubmissionReader;
import com.calata.evaluator.submission.api.application.port.out.SubmissionWriter;
import com.calata.evaluator.submission.api.domain.model.submission.Submission;
import com.calata.evaluator.submission.api.domain.model.submission.SubmissionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionWriter submissionWriter;

    @Mock
    private SubmissionEventsPublisher events;

    @Mock
    private SubmissionReader submissionReader;

    @InjectMocks
    private SubmissionService service;

    @Test
    void handle_shouldCreateSubmissionPublishEventPersistAndReturnId() {
        // given
        String userId = "user-1";
        String title = "My submission";
        String code = "public class Main {}";
        Language language = Language.JAVA;

        CreateSubmissionCommand cmd = mock(CreateSubmissionCommand.class);
        when(cmd.userId()).thenReturn(userId);
        when(cmd.title()).thenReturn(title);
        when(cmd.code()).thenReturn(code);
        when(cmd.language()).thenReturn(language);

        Submission submission = mock(Submission.class);
        when(submission.getId()).thenReturn("sub-123");

        try (MockedStatic<Submission> mockedStatic = mockStatic(Submission.class)) {
            mockedStatic.when(() -> Submission.create(userId, title, code, language))
                    .thenReturn(submission);

            // when
            String resultId = service.handle(cmd);

            // then
            mockedStatic.verify(() -> Submission.create(userId, title, code, language));
            verify(events).publishSubmission(submission);
            verify(submissionWriter).save(submission);

            assertEquals("sub-123", resultId);
        }
    }

    @Test
    void updateSubmissionStatus_shouldUpdateStatusPublishEventAndReturnId() {
        // given
        String submissionId = "sub-456";
        String status = "COMPLETED";

        Submission updated = mock(Submission.class);
        when(updated.getId()).thenReturn(submissionId);

        when(submissionWriter.updateStatus(submissionId, status)).thenReturn(updated);

        // when
        String result = service.updateSubmissionStatus(submissionId, status);

        // then
        verify(submissionWriter).updateStatus(submissionId, status);
        verify(events).publishSubmissionStatusUpdated(updated);
        assertEquals(submissionId, result);
    }

    @Test
    void getSubmission_shouldReadFromRepositoryAndMapToDto() {
        // given
        String submissionId = "sub-789";
        Submission submission = mock(Submission.class);

        Instant createdAt = Instant.parse("2024-01-01T10:15:30Z");

        when(submission.getId()).thenReturn(submissionId);
        when(submission.getTitle()).thenReturn("Title");
        when(submission.getLanguage()).thenReturn(Language.JAVA);
        when(submission.getCode()).thenReturn("code");
        when(submission.getStatus()).thenReturn(SubmissionStatus.PENDING);
        when(submission.getCreatedAt()).thenReturn(createdAt);
        when(submission.getUserId()).thenReturn("user-1");

        when(submissionReader.getById(submissionId)).thenReturn(submission);

        // when
        SubmissionResponse dto = service.getSubmission(submissionId);

        // then
        verify(submissionReader).getById(submissionId);

        assertEquals(submissionId, dto.id());
        assertEquals("Title", dto.title());
        assertEquals(Language.JAVA.name(), dto.language());
        assertEquals("code", dto.code());
        assertEquals(SubmissionStatus.PENDING.name(), dto.status());
        assertEquals(createdAt.toString(), dto.createdAt());
        assertEquals("user-1", dto.userId());
    }

    @Test
    void getSubmissionByUserId_shouldMapAllSubmissionsToDtos() {
        // given
        String userId = "user-42";

        Submission s1 = mock(Submission.class);
        Submission s2 = mock(Submission.class);

        when(s1.getId()).thenReturn("sub-1");
        when(s1.getTitle()).thenReturn("First");
        when(s1.getLanguage()).thenReturn(Language.JAVA);
        when(s1.getCode()).thenReturn("code1");
        when(s1.getStatus()).thenReturn(SubmissionStatus.PENDING);
        when(s1.getCreatedAt()).thenReturn(Instant.parse("2024-01-01T10:00:00Z"));
        when(s1.getUserId()).thenReturn(userId);

        when(s2.getId()).thenReturn("sub-2");
        when(s2.getTitle()).thenReturn("Second");
        when(s2.getLanguage()).thenReturn(Language.PYTHON);
        when(s2.getCode()).thenReturn("code2");
        when(s2.getStatus()).thenReturn(SubmissionStatus.COMPLETED);
        when(s2.getCreatedAt()).thenReturn(Instant.parse("2024-01-02T11:00:00Z"));
        when(s2.getUserId()).thenReturn(userId);

        when(submissionReader.getByUserId(userId)).thenReturn(List.of(s1, s2));

        // when
        List<SubmissionResponse> dtos = service.getSubmissionByUserId(userId);

        // then
        verify(submissionReader).getByUserId(userId);
        assertEquals(2, dtos.size());

        SubmissionResponse dto1 = dtos.get(0);
        assertEquals("sub-1", dto1.id());
        assertEquals("First", dto1.title());
        assertEquals(Language.JAVA.name(), dto1.language());
        assertEquals("code1", dto1.code());
        assertEquals(SubmissionStatus.PENDING.name(), dto1.status());
        assertEquals("2024-01-01T10:00:00Z", dto1.createdAt());
        assertEquals(userId, dto1.userId());

        SubmissionResponse dto2 = dtos.get(1);
        assertEquals("sub-2", dto2.id());
        assertEquals("Second", dto2.title());
        assertEquals(Language.PYTHON.name(), dto2.language());
        assertEquals("code2", dto2.code());
        assertEquals(SubmissionStatus.COMPLETED.name(), dto2.status());
        assertEquals("2024-01-02T11:00:00Z", dto2.createdAt());
        assertEquals(userId, dto2.userId());
    }
}
