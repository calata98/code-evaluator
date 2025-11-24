package com.calata.evaluator.user.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BCryptPasswordHasherTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private BCryptPasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new BCryptPasswordHasher(passwordEncoder);
    }

    @Test
    void hash_shouldDelegateToPasswordEncoderEncode() {
        // given
        String raw = "my-secret";
        String encoded = "$2a$10$hashhashhash";

        when(passwordEncoder.encode(raw)).thenReturn(encoded);

        // when
        String result = hasher.hash(raw);

        // then
        assertThat(result).isEqualTo(encoded);
        verify(passwordEncoder).encode(raw);
    }

    @Test
    void matches_shouldDelegateToPasswordEncoderMatches() {
        // given
        String raw = "my-secret";
        String hash = "$2a$10$hashhashhash";

        when(passwordEncoder.matches(raw, hash)).thenReturn(true);

        // when
        boolean result = hasher.matches(raw, hash);

        // then
        assertThat(result).isTrue();
        verify(passwordEncoder).matches(raw, hash);
    }
}
