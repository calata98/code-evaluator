package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.submission.api.domain.model.summary.SubmissionSummary;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewDocument;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MySubmissionsQueryControllerTest {

    @Mock
    private SubmissionDetailViewRepository repo;

    @InjectMocks
    private MySubmissionsQueryController controller;

    @Test
    void listMine_shouldQueryByJwtSubjectAndReturnResults() {
        // given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("user-123");

        int page = 1;
        int size = 10;

        SubmissionDetailViewDocument doc1 = mock(SubmissionDetailViewDocument.class);
        SubmissionDetailViewDocument doc2 = mock(SubmissionDetailViewDocument.class);
        List<SubmissionDetailViewDocument> expectedList = List.of(doc1, doc2);

        when(repo.findBySubmissionUserIdOrderBySubmissionCreatedAtDesc(
                eq("user-123"),
                any(PageRequest.class))
        ).thenReturn(expectedList);

        // when
        List<SubmissionDetailViewDocument> result = controller.listMine(jwt, page, size);

        // then
        assertSame(expectedList, result);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(repo).findBySubmissionUserIdOrderBySubmissionCreatedAtDesc(eq("user-123"), pageCaptor.capture());

        PageRequest pr = pageCaptor.getValue();
        assertEquals(page, pr.getPageNumber());
        assertEquals(size, pr.getPageSize());
    }

    @Test
    void getOne_shouldReturnViewWhenUserOwnsSubmission() {
        // given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("user-123");

        SubmissionSummary summary = new SubmissionSummary(
                "sub-1",
                "user-123",
                "PENDING",
                "Title",
                "JAVA",
                Instant.parse("2024-01-01T10:00:00Z"),
                false,
                false,
                null,
                null,
                null
        );
        SubmissionDetailViewDocument view =
                new SubmissionDetailViewDocument("sub-1", summary, List.of(), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(view));

        // when
        SubmissionDetailViewDocument result = controller.getOne(jwt, "sub-1");

        // then
        assertSame(view, result);
    }

    @Test
    void getOne_shouldThrowForbiddenWhenUserDoesNotOwnSubmission() {
        // given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("user-123");

        SubmissionSummary summary = new SubmissionSummary(
                "sub-1",
                "other-user",
                "PENDING",
                "Title",
                "JAVA",
                Instant.parse("2024-01-01T10:00:00Z"),
                false,
                false,
                null,
                null,
                null
        );
        SubmissionDetailViewDocument view =
                new SubmissionDetailViewDocument("sub-1", summary, List.of(), Instant.now());

        when(repo.findById("sub-1")).thenReturn(Optional.of(view));

        // when / then
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getOne(jwt, "sub-1")
        );

        HttpStatusCode status = ex.getStatusCode();
        assertEquals(403, status.value());
    }

    @Test
    void getOne_whenNotFoundShouldThrowNoSuchElementException() {
        // given
        Jwt jwt = mock(Jwt.class);
        when(repo.findById("missing")).thenReturn(Optional.empty());

        // when / then
        assertThrows(NoSuchElementException.class, () -> controller.getOne(jwt, "missing"));
    }
}
