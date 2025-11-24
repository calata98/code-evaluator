package com.calata.evaluator.user.infrastructure.web.handler;
import com.calata.evaluator.user.infrastructure.web.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuthExceptionHandlerTest {

    private AuthExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new AuthExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    void handleIllegalArgument_returnsBadRequestWithExceptionMessage() {
        // given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid credentials");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgument(ex, request);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        // Ajusta estos accesores a tu implementación (message() / getMessage() / etc.)
        assertEquals("Invalid credentials", body.message());
        assertEquals("/api/auth/login", body.path());
    }

    @Test
    void handleNotFound_returnsNotFoundWithGenericMessage() {
        // given
        NoSuchElementException ex = new NoSuchElementException("User not found");
        when(request.getRequestURI()).thenReturn("/api/auth/user/123");

        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(ex, request);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Resource not found", body.message());
        assertEquals("/api/auth/user/123", body.path());
    }

    @Test
    void handleUnexpected_returnsInternalServerErrorWithGenericMessage() {
        // given
        Exception ex = new RuntimeException("Something broke internally");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpected(ex, request);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Unexpected server error", body.message());
        assertEquals("/api/auth/login", body.path());
    }
}
