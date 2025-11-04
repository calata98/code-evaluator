package com.calata.evaluator.user.infrastructure.security;

import com.calata.evaluator.user.application.port.out.TokenStore;
import com.calata.evaluator.user.infrastructure.security.doc.RevokedTokenDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class TokenStoreMongoAdapter implements TokenStore {

    private final MongoTemplate template;

    public TokenStoreMongoAdapter(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public void revoke(String token, Instant expiresAt) {
        template.save(new RevokedTokenDocument(token, expiresAt));
    }

    @Override
    public boolean isRevoked(String token) {
        return template.exists(new Query(where("_id").is(token)), RevokedTokenDocument.class);
    }
}
