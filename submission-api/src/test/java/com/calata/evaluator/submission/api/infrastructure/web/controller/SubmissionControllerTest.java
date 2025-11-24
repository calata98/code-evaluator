package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.contracts.dto.SubmissionCodeResponse;
import com.calata.evaluator.contracts.dto.SubmissionResponse;
import com.calata.evaluator.contracts.dto.UpdateSubmissionStatus;
import com.calata.evaluator.contracts.types.Language;
import com.calata.evaluator.submission.api.application.command.CreateSubmissionCommand;
import com.calata.evaluator.submission.api.application.command.UpdateSubmissionStatusCommand;
import com.calata.evaluator.submission.api.application.port.in.CreateSubmissionUseCase;
import com.calata.evaluator.submission.api.application.port.in.GetSubmissionUseCase;
import com.calata.evaluator.submission.api.application.port.in.UpdateSubmissionStatusUseCase;
import com.calata.evaluator.submission.api.infrastructure.web.dto.SubmissionIdResponse;
import com.calata.evaluator.submission.api.infrastructure.web.dto.SubmissionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    @Mock
    private CreateSubmissionUseCase createSubmission;

    @Mock
    private UpdateSubmissionStatusUseCase updateSubmissionStatus;

    @Mock
    private GetSubmissionUseCase getSubmission;

    @Mock
    private Jwt jwt;

    @Captor
    private ArgumentCaptor<CreateSubmissionCommand> createCommandCaptor;

    @Captor
    private ArgumentCaptor<UpdateSubmissionStatusCommand> updateStatusCommandCaptor;

    private SubmissionController controller;

    @BeforeEach
    void setUp() {
        controller = new SubmissionController(createSubmission, updateSubmissionStatus, getSubmission);
    }

    @Test
    void getById_shouldReturnOkWithSubmission() {
        // given
        String id = "sub-123";
        SubmissionResponse submissionResponse = mock(SubmissionResponse.class);
        when(getSubmission.getSubmission(id)).thenReturn(submissionResponse);

        // when
        ResponseEntity<SubmissionResponse> response = controller.getById(id);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(submissionResponse);
        verify(getSubmission).getSubmission(id);
    }

    @Test
    void getByUserId_shouldUseJwtSubjectAndReturnOkWithList() {
        // given
        String userId = "user-123";
        when(jwt.getSubject()).thenReturn(userId);

        SubmissionResponse s1 = mock(SubmissionResponse.class);
        SubmissionResponse s2 = mock(SubmissionResponse.class);
        List<SubmissionResponse> list = List.of(s1, s2);

        when(getSubmission.getSubmissionByUserId(userId)).thenReturn(list);

        // when
        ResponseEntity<List<SubmissionResponse>> response = controller.getByUserId(jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(s1, s2);
        verify(getSubmission).getSubmissionByUserId(userId);
    }

    @Test
    void create_shouldBuildCommandFromJwtAndRequestAndReturnAcceptedWithId() {
        // given
        String userId = "user-123";
        when(jwt.getSubject()).thenReturn(userId);

        String title = "My submission";
        String code = "public class Main {}";
        Language language = Language.JAVA;

        SubmissionRequest req = mock(SubmissionRequest.class);
        when(req.title()).thenReturn(title);
        when(req.code()).thenReturn(code);
        when(req.language()).thenReturn(language);

        String generatedId = "sub-999";
        when(createSubmission.handle(any(CreateSubmissionCommand.class))).thenReturn(generatedId);

        // when
        ResponseEntity<SubmissionIdResponse> response = controller.create(jwt, req);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().submissionId()).isEqualTo(generatedId);

        // opcional: capturar el comando para validar sus campos
        verify(createSubmission).handle(createCommandCaptor.capture());
        CreateSubmissionCommand cmd = createCommandCaptor.getValue();
        assertThat(cmd.userId()).isEqualTo(userId);
        assertThat(cmd.title()).isEqualTo(title);
        assertThat(cmd.code()).isEqualTo(code);
        assertThat(cmd.language()).isEqualTo(language);
    }

    @Test
    void updateStatus_shouldCallUseCaseAndReturnAcceptedWithId() {
        // given
        String submissionId = "sub-123";
        String status = "RUNNING"; // ajusta según tipo (enum, etc.)

        UpdateSubmissionStatus req = mock(UpdateSubmissionStatus.class);
        when(req.submissionId()).thenReturn(submissionId);
        when(req.status()).thenReturn(status);

        String updatedId = submissionId;
        when(updateSubmissionStatus.updateSubmissionStatus(submissionId, status)).thenReturn(updatedId);

        // when
        ResponseEntity<SubmissionIdResponse> response = controller.updateStatus(req);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().submissionId()).isEqualTo(updatedId);

        verify(updateSubmissionStatus).updateSubmissionStatus(submissionId, status);
    }

    @Test
    void getSubmissionCode_whenSubmissionExists_shouldReturnOkWithCode() {
        // given
        String id = "sub-123";
        String code = "System.out.println(\"hello\");";

        SubmissionResponse submissionResponse = mock(SubmissionResponse.class);
        when(submissionResponse.id()).thenReturn(id);
        when(submissionResponse.code()).thenReturn(code);

        when(getSubmission.getSubmission(id)).thenReturn(submissionResponse);

        // when
        ResponseEntity<SubmissionCodeResponse> response = controller.getSubmissionCode(id);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        assertThat(response.getBody().code()).isEqualTo(code);

        verify(getSubmission).getSubmission(id);
    }

    @Test
    void getSubmissionCode_whenSubmissionDoesNotExist_shouldReturnNotFound() {
        // given
        String id = "sub-404";
        when(getSubmission.getSubmission(id)).thenReturn(null);

        // when
        ResponseEntity<SubmissionCodeResponse> response = controller.getSubmissionCode(id);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(getSubmission).getSubmission(id);
    }
}
