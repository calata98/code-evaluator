package com.calata.evaluator.similarity.infrastructure.repo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("code_fingerprints")
@CompoundIndexes({
        @CompoundIndex(name = "shaNorm_createdAt_idx", def = "{ 'shaNorm': 1, 'createdAt': -1 }"),
        @CompoundIndex(name = "shaRaw_createdAt_idx",  def = "{ 'shaRaw': 1,  'createdAt': -1 }")
})
@Data
public class FingerprintDocument {
    @Id
    private String submissionId;
    private String userId;
    private String language;
    @Indexed
    private String shaRaw;
    @Indexed
    private String shaNorm;
    private long simhash64;
    private int lineCount;
    private Instant createdAt;
}

