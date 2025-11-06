package com.calata.evaluator.user.infrastructure.repo;

import com.calata.evaluator.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("users")
@CompoundIndex(name="uq_email", def="{ 'email': 1 }", unique=true)
@Data
@AllArgsConstructor
public class UserDocument {

    @Id
    private String id;
    private String email;
    private String passwordHash;
    private Role role;
    private Instant createdAt;
}
