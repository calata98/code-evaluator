package com.calata.evaluator.submission.api.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String expected;
    private final AntPathRequestMatcher matcher =
            new AntPathRequestMatcher("/submissions/status", "PUT"); // tolera context-path

    public InternalApiKeyFilter(@Value("${internal.api.key}") String expected) {
        this.expected = expected;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {

        if (matcher.matches(req)) {
            String key = req.getHeader("X-Internal-Api-Key");
            if (key == null || !key.equals(expected)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
