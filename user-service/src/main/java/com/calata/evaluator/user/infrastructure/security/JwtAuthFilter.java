package com.calata.evaluator.user.infrastructure.security;

import com.calata.evaluator.user.application.port.out.TokenEncoder;
import com.calata.evaluator.user.application.port.out.TokenStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenEncoder tokenEncoder;
    private final TokenStore tokenStore;

    public JwtAuthFilter(TokenEncoder tokenEncoder, TokenStore tokenStore) {
        this.tokenEncoder = tokenEncoder;
        this.tokenStore = tokenStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String hdr = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (hdr != null && hdr.startsWith("Bearer ")) {
            String token = hdr.substring(7);
            if (!tokenEncoder.isValid(token) || tokenStore.isRevoked(token)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getServletPath();
        return "OPTIONS".equalsIgnoreCase(req.getMethod())
                || p.startsWith("/api/auth/")
                || p.startsWith("/.well-known/")
                || p.startsWith("/oauth2/");
    }
}
