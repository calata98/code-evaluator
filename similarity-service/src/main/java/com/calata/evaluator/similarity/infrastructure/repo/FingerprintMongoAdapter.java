package com.calata.evaluator.similarity.infrastructure.repo;

import com.calata.evaluator.similarity.application.port.out.FingerprintReader;
import com.calata.evaluator.similarity.application.port.out.FingerprintWriter;
import com.calata.evaluator.similarity.domain.model.Fingerprint;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FingerprintMongoAdapter implements FingerprintWriter, FingerprintReader {

    private final SpringDataFingerprintRepository repo;

    public FingerprintMongoAdapter(SpringDataFingerprintRepository repo) {
        this.repo = repo;
    }

    @Override
    public void upsert(Fingerprint fp) {
        repo.save(Mappers.toDocument(fp));
    }

    @Override
    public Optional<Fingerprint> findByShaRaw(String shaRaw) {
        return Mappers.fingerprintToDomain(repo.findFirstByShaRaw(shaRaw));
    }

    @Override
    public Optional<Fingerprint> findByShaNorm(String shaNorm) {
        return Mappers.fingerprintToDomain(repo.findFirstByShaNorm(shaNorm));
    }

    @Override
    public List<Fingerprint> findRecentByLangAndSize(String lang, int lineCount, int limit, double tolerance) {
        int min = (int)Math.floor(lineCount * (1.0 - tolerance));
        int max = (int)Math.ceil(lineCount * (1.0 + tolerance));
        var list = repo.findByLangAndCountBetween(lang, min, max, Sort.by(Sort.Direction.DESC, "createdAt"));
        var mapped = Mappers.fingerprintsToDomain(list);
        return mapped.size() > limit ? mapped.subList(0, limit) : mapped;
    }
}
