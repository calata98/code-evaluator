package com.calata.evaluator.evaluation.orchestrator.infrastructure.submission;

import com.calata.evaluator.contracts.dto.SubmissionResponse;
import com.calata.evaluator.contracts.dto.UpdateSubmissionStatus;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.SubmissionReader;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.config.SubmissionApiProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionStatusRestAdapterTest {

    @Mock
    private WebClient.Builder builder;

    @Mock
    private WebClient webClient;

    @Captor
    private ArgumentCaptor<UpdateSubmissionStatus> statusCaptor;

    private SubmissionStatusRestAdapter adapter;

    @BeforeEach
    void setUp() {
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        SubmissionApiProps props = new SubmissionApiProps("http://submission-api");
        adapter = new SubmissionStatusRestAdapter(builder, props);
        adapter.apiKey = "secret-key";
    }

    // ------------------------------
    // markRunning()
    // ------------------------------

    @Test
    void markRunning_shouldSendPutWithCorrectBody() {
        WebClient.RequestBodyUriSpec putSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.put()).thenReturn(putSpec);
        when(putSpec.uri("/submissions/status")).thenReturn(putSpec);
        when(putSpec.header("X-Internal-Api-Key", "secret-key")).thenReturn(putSpec);
        when(putSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(putSpec);
        when(putSpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        adapter.markRunning("sub-123");

        verify(putSpec).bodyValue(statusCaptor.capture());
        UpdateSubmissionStatus body = statusCaptor.getValue();

        assertThat(body.submissionId()).isEqualTo("sub-123");
        assertThat(body.status()).isEqualTo("RUNNING");
    }

    // ------------------------------
    // findById() success
    // ------------------------------

    @Test
    void findById_shouldReturnSnapshotBasedOnSubmissionResponse() {
        String submissionId = "sub-99";

        SubmissionResponse dto = new SubmissionResponse(
                submissionId,
                "Some title",
                "java",
                "System.out.println()",
                "CREATED",
                "2024-01-01T00:00:00Z",
                "user-1"
        );

        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono monoMock = mock(Mono.class);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri("/submissions/{id}", submissionId)).thenReturn(headersSpec);
        when(headersSpec.header("X-Internal-Api-Key", "secret-key")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SubmissionResponse.class)).thenReturn(monoMock);
        when(monoMock.block()).thenReturn(dto);

        SubmissionReader.SubmissionSnapshot snapshot = adapter.findById(submissionId);

        assertThat(snapshot.id()).isEqualTo(submissionId);
        assertThat(snapshot.language()).isEqualTo("java");
        assertThat(snapshot.code()).isEqualTo("System.out.println()");
        assertThat(snapshot.userId()).isEqualTo("user-1");
    }

    // ------------------------------
    // findById() null → throws
    // ------------------------------

    @Test
    void findById_whenNullResponse_shouldThrow() {
        String submissionId = "sub-404";

        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono monoMock = mock(Mono.class);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri("/submissions/{id}", submissionId)).thenReturn(headersSpec);
        when(headersSpec.header("X-Internal-Api-Key", "secret-key")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SubmissionResponse.class)).thenReturn(monoMock);
        when(monoMock.block()).thenReturn(null);

        assertThatThrownBy(() -> adapter.findById(submissionId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Submission not found: sub-404");
    }
}
