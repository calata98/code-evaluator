package com.calata.evaluator.user.infrastructure.security;

import com.calata.evaluator.user.application.port.out.TokenEncoder;
import com.calata.evaluator.user.application.port.out.TokenStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private TokenEncoder tokenEncoder;

    @Mock
    private TokenStore tokenStore;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private FilterChain chain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(tokenEncoder, tokenStore);
    }

    // ----------------------------------------------------------------------
    // doFilterInternal
    // ----------------------------------------------------------------------

    @Test
    void doFilterInternal_validToken_shouldContinueFilterChain() throws ServletException, IOException {
        when(req.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(tokenEncoder.notValid("abc123")).thenReturn(false);
        when(tokenStore.isRevoked("abc123")).thenReturn(false);

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_invalidToken_shouldReturn401() throws ServletException, IOException {
        when(req.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(tokenEncoder.notValid("bad-token")).thenReturn(true);

        filter.doFilterInternal(req, res, chain);

        verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(req, res);
    }

    @Test
    void doFilterInternal_revokedToken_shouldReturn401() throws ServletException, IOException {
        when(req.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(tokenEncoder.notValid("abc123")).thenReturn(false);
        when(tokenStore.isRevoked("abc123")).thenReturn(true);

        filter.doFilterInternal(req, res, chain);

        verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(req, res);
    }

    @Test
    void doFilterInternal_noToken_shouldContinueChain() throws ServletException, IOException {
        when(req.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).setStatus(anyInt());
    }

    // ----------------------------------------------------------------------
    // shouldNotFilter
    // ----------------------------------------------------------------------

    @Test
    void shouldNotFilter_optionsRequest() {
        when(req.getMethod()).thenReturn("OPTIONS");
        when(req.getServletPath()).thenReturn("/anything");

        assertThat(filter.shouldNotFilter(req)).isTrue();
    }

    @Test
    void shouldNotFilter_authEndpoints() {
        when(req.getMethod()).thenReturn("GET");
        when(req.getServletPath()).thenReturn("/api/auth/login");

        assertThat(filter.shouldNotFilter(req)).isTrue();
    }

    @Test
    void shouldNotFilter_wellKnown() {
        when(req.getMethod()).thenReturn("GET");
        when(req.getServletPath()).thenReturn("/.well-known/openid-configuration");

        assertThat(filter.shouldNotFilter(req)).isTrue();
    }

    @Test
    void shouldNotFilter_oauth2() {
        when(req.getMethod()).thenReturn("GET");
        when(req.getServletPath()).thenReturn("/oauth2/authorize");

        assertThat(filter.shouldNotFilter(req)).isTrue();
    }

    @Test
    void shouldNotFilter_otherPaths_shouldBeFiltered() {
        when(req.getMethod()).thenReturn("GET");
        when(req.getServletPath()).thenReturn("/api/submissions/123");

        assertThat(filter.shouldNotFilter(req)).isFalse();
    }
}
