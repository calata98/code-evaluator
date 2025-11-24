package com.calata.evaluator.submission.api.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGeneric_whenAcceptIsSse_shouldReturnSsePayload() {
        // given
        Exception ex = new RuntimeException("boom");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(MediaType.TEXT_EVENT_STREAM_VALUE);

        // when
        ResponseEntity<?> response = handler.handleGeneric(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_EVENT_STREAM);

        Object body = response.getBody();
        assertThat(body).isInstanceOf(String.class);
        assertThat((String) body)
                .isEqualTo("event: error\n" +
                        "data: Unexpected error\n\n");
    }

    @Test
    void handleGeneric_whenAcceptIsNotSse_shouldReturnJsonErrorResponse() {
        // given
        Exception ex = new RuntimeException("boom");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        // when
        ResponseEntity<?> response = handler.handleGeneric(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        Object body = response.getBody();
        assertThat(body).isInstanceOf(ErrorResponse.class);

        ErrorResponse errorResponse = (ErrorResponse) body;
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(errorResponse.getBody().getDetail()).isEqualTo("Unexpected error");
    }

    @Test
    void handleGeneric_whenAcceptHeaderIsNull_shouldReturnJsonErrorResponse() {
        // given
        Exception ex = new RuntimeException("boom");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(null);

        // when
        ResponseEntity<?> response = handler.handleGeneric(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        Object body = response.getBody();
        assertThat(body).isInstanceOf(ErrorResponse.class);

        ErrorResponse errorResponse = (ErrorResponse) body;
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(errorResponse.getBody().getDetail()).isEqualTo("Unexpected error");
    }
}
