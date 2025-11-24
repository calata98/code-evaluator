package com.calata.evaluator.user.infrastructure.repo.mapper;

import com.calata.evaluator.user.domain.model.Role;
import com.calata.evaluator.user.domain.model.User;
import com.calata.evaluator.user.infrastructure.repo.UserDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceMapperTest {

    @Test
    void toDoc_shouldMapDomainUserToUserDocument() {
        // given
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        User user = new User(
                "u1",
                "test@example.com",
                "hashed-pass",
                Role.ADMIN,
                createdAt
        );

        // when
        UserDocument doc = UserPersistenceMapper.toDoc(user);

        // then
        assertThat(doc.getId()).isEqualTo("u1");
        assertThat(doc.getEmail()).isEqualTo("test@example.com");
        assertThat(doc.getPasswordHash()).isEqualTo("hashed-pass");
        assertThat(doc.getRole()).isEqualTo(Role.ADMIN);
        assertThat(doc.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void toDomain_shouldMapUserDocumentToDomainUser() {
        // given
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        UserDocument doc = new UserDocument(
                "u2",
                "user2@example.com",
                "other-hash",
                Role.USER,
                createdAt
        );

        // when
        User user = UserPersistenceMapper.toDomain(doc);

        // then
        assertThat(user.id()).isEqualTo("u2");
        assertThat(user.email()).isEqualTo("user2@example.com");
        assertThat(user.passwordHash()).isEqualTo("other-hash");
        assertThat(user.role()).isEqualTo(Role.USER);
        assertThat(user.createdAt()).isEqualTo(createdAt);
    }
}
