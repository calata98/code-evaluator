package com.calata.evaluator.user.infrastructure.security;

import com.calata.evaluator.user.infrastructure.security.doc.RevokedTokenDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

class TokenStoreMongoAdapterTest {

    private MongoTemplate mongoTemplate;
    private TokenStoreMongoAdapter adapter;

    @BeforeEach
    void setup() {
        mongoTemplate = mock(MongoTemplate.class);
        adapter = new TokenStoreMongoAdapter(mongoTemplate);
    }

    @Test
    void revoke_savesRevokedTokenDocument() {
        // given
        String token = "abc123";
        Instant expiresAt = Instant.parse("2025-11-23T10:00:00Z");

        // when
        adapter.revoke(token, expiresAt);

        // then
        ArgumentCaptor<RevokedTokenDocument> captor = ArgumentCaptor.forClass(RevokedTokenDocument.class);
        verify(mongoTemplate).save(captor.capture());

        RevokedTokenDocument saved = captor.getValue();
        assertEquals(token, saved.getToken());
        assertEquals(expiresAt, saved.getExpiresAt());
    }

    @Test
    void isRevoked_returnsTrue_whenDocumentExists() {
        // given
        String token = "revoked-token";

        when(mongoTemplate.exists(any(Query.class), eq(RevokedTokenDocument.class)))
                .thenReturn(true);

        // when
        boolean result = adapter.isRevoked(token);

        // then
        assertTrue(result);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).exists(queryCaptor.capture(), eq(RevokedTokenDocument.class));

        Query sentQuery = queryCaptor.getValue();
        assertEquals(where("_id").is(token).getCriteriaObject(), sentQuery.getQueryObject());
    }

    @Test
    void isRevoked_returnsFalse_whenDocumentDoesNotExist() {
        // given
        String token = "not-revoked";

        when(mongoTemplate.exists(any(Query.class), eq(RevokedTokenDocument.class)))
                .thenReturn(false);

        // when
        boolean result = adapter.isRevoked(token);

        // then
        assertFalse(result);
    }
}
