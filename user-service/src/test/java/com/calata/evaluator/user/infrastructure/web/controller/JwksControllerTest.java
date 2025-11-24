package com.calata.evaluator.user.infrastructure.web.controller;
import com.calata.evaluator.user.infrastructure.security.JwksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwksControllerTest {

    @Mock
    private JwksService jwksService;

    private JwksController controller;

    @BeforeEach
    void setUp() {
        controller = new JwksController(jwksService);
    }

    @Test
    void jwks_shouldReturnJwkJsonObjectWithCacheHeaders() {
        // given
        Map<String, Object> jwksObject = Map.of();
        when(jwksService.jwksJsonObject()).thenReturn(jwksObject);

        // when
        ResponseEntity<?> response = controller.jwks();

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(jwksObject);

        assertThat(response.getHeaders().getCacheControl())
                .isEqualTo(CacheControl.maxAge(300, TimeUnit.SECONDS).getHeaderValue());

        verify(jwksService).jwksJsonObject();
    }
}
