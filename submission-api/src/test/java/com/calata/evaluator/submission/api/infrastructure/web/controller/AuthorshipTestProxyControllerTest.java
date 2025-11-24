package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.contracts.dto.AuthorshipEvaluationView;
import com.calata.evaluator.contracts.dto.AuthorshipTestView;
import com.calata.evaluator.contracts.dto.SubmitAnswersRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorshipTestProxyControllerTest {

    @Mock
    private WebClient webClient;

    private AuthorshipTestProxyController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthorshipTestProxyController(webClient);
    }

    @Test
    void getTest_shouldProxyCallToAuthorshipServiceWithAuthHeader() {
        // given
        String id = "test-123";
        String authHeader = "Bearer token";

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        AuthorshipTestView body = mock(AuthorshipTestView.class);
        ResponseEntity<AuthorshipTestView> responseEntity = ResponseEntity.ok(body);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(eq("/authorship-tests/{id}"), eq(id))).thenReturn(headersSpec);
        when(headersSpec.header(HttpHeaders.AUTHORIZATION, authHeader)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(AuthorshipTestView.class))
                .thenReturn(Mono.just(responseEntity));

        // when
        ResponseEntity<AuthorshipTestView> result =
                controller.getTest(id, authHeader).block();

        // then
        assertEquals(responseEntity, result);
        verify(webClient).get();
        verify(getSpec).uri("/authorship-tests/{id}", id);
        verify(headersSpec).header(HttpHeaders.AUTHORIZATION, authHeader);
        verify(headersSpec).retrieve();
        verify(responseSpec).toEntity(AuthorshipTestView.class);
    }

    @Test
    void submitAnswers_shouldProxyPostWithBodyAndHeaders() {
        // given
        String id = "test-456";
        String authHeader = "Bearer token";
        SubmitAnswersRequest requestBody = mock(SubmitAnswersRequest.class);

        @SuppressWarnings("rawtypes")
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        ResponseEntity<Void> responseEntity = ResponseEntity.noContent().build();

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(eq("/authorship-tests/{id}/answers"), eq(id))).thenReturn(postSpec);
        when(postSpec.header(HttpHeaders.AUTHORIZATION, authHeader)).thenReturn(postSpec);
        when(postSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(postSpec);
        when(postSpec.bodyValue(requestBody)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenReturn(Mono.just(responseEntity));

        // when
        ResponseEntity<Void> result =
                controller.submitAnswers(id, authHeader, requestBody).block();

        // then
        assertEquals(responseEntity, result);
        verify(webClient).post();
        verify(postSpec).uri("/authorship-tests/{id}/answers", id);
        verify(postSpec).header(HttpHeaders.AUTHORIZATION, authHeader);
        verify(postSpec).contentType(MediaType.APPLICATION_JSON);
        verify(postSpec).bodyValue(requestBody);
        verify(headersSpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void getEvaluations_shouldProxyCallToAuthorshipServiceWithAuthHeader() {
        // given
        String id = "eval-123";
        String authHeader = "Bearer token";

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        AuthorshipEvaluationView body = mock(AuthorshipEvaluationView.class);
        ResponseEntity<AuthorshipEvaluationView> responseEntity = ResponseEntity.ok(body);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(eq("/authorship-evaluations/{id}"), eq(id))).thenReturn(headersSpec);
        when(headersSpec.header(HttpHeaders.AUTHORIZATION, authHeader)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(AuthorshipEvaluationView.class))
                .thenReturn(Mono.just(responseEntity));

        // when
        ResponseEntity<AuthorshipEvaluationView> result =
                controller.getEvaluations(id, authHeader).block();

        // then
        assertEquals(responseEntity, result);
        verify(webClient).get();
        verify(getSpec).uri("/authorship-evaluations/{id}", id);
        verify(headersSpec).header(HttpHeaders.AUTHORIZATION, authHeader);
        verify(headersSpec).retrieve();
        verify(responseSpec).toEntity(AuthorshipEvaluationView.class);
    }
}
