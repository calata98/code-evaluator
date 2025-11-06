package com.calata.evaluator.submission.api.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http, InternalApiKeyFilter apiKeyFilter) throws Exception {
        var statusMatcher = new AntPathRequestMatcher("/submissions/status", "PUT");

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(statusMatcher))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/submissions/status", "/submissions/status/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/submissions/*").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/submissions/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

        http.addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${front.origin:http://localhost:5173}") String frontOrigin) {
        var conf = new CorsConfiguration();
        conf.setAllowedOrigins(List.of(frontOrigin));
        conf.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        conf.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","X-Internal-Api-Key"));
        conf.setMaxAge(3600L);
        conf.setAllowCredentials(false);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", conf);
        return source;
    }
}
