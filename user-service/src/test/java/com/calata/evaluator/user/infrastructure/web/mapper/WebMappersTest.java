package com.calata.evaluator.user.infrastructure.web.mapper;

import com.calata.evaluator.user.domain.model.Role;
import com.calata.evaluator.user.domain.model.User;
import com.calata.evaluator.user.infrastructure.web.dto.UserResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebMappersTest {

    // -----------------------------------------------------
    // toRole()
    // -----------------------------------------------------

    @Test
    void toRole_nullString_shouldReturnUser() {
        Role result = WebMappers.toRole(null);
        assertThat(result).isEqualTo(Role.USER);
    }

    @Test
    void toRole_lowercase_shouldConvertToEnum() {
        Role result = WebMappers.toRole("admin");
        assertThat(result).isEqualTo(Role.ADMIN);
    }

    @Test
    void toRole_uppercase_shouldConvertDirectly() {
        Role result = WebMappers.toRole("USER");
        assertThat(result).isEqualTo(Role.USER);
    }

    // -----------------------------------------------------
    // toUserResponse()
    // -----------------------------------------------------

    @Test
    void toUserResponse_shouldMapFieldsCorrectly() {
        // given
        User user = mock(User.class);
        when(user.id()).thenReturn("u1");
        when(user.email()).thenReturn("test@example.com");
        when(user.role()).thenReturn(Role.ADMIN);
        when(user.createdAt()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));

        // when
        UserResponse res = WebMappers.toUserResponse(user);

        // then
        assertThat(res.id()).isEqualTo("u1");
        assertThat(res.email()).isEqualTo("test@example.com");
        assertThat(res.role()).isEqualTo("ADMIN");
        assertThat(res.createdAt()).isEqualTo("2024-01-01T00:00:00Z");
    }
}
