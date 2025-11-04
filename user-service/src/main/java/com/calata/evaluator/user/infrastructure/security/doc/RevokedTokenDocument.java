package com.calata.evaluator.user.infrastructure.security.doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("revoked_tokens")
@Data
@AllArgsConstructor
public class RevokedTokenDocument {
    @Id
    private String token;
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}
