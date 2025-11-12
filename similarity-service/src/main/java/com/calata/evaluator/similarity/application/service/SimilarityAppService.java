package com.calata.evaluator.similarity.application.service;

import com.calata.evaluator.similarity.application.command.ProcessEvaluationCompletedCommand;
import com.calata.evaluator.similarity.application.port.in.HandleEvaluationCompletedUseCase;
import com.calata.evaluator.similarity.application.port.out.DomainEventPublisher;
import com.calata.evaluator.similarity.application.port.out.FingerprintReader;
import com.calata.evaluator.similarity.application.port.out.FingerprintWriter;
import com.calata.evaluator.similarity.application.port.out.SimilarityResultWriter;
import com.calata.evaluator.similarity.domain.model.Fingerprint;
import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import com.calata.evaluator.similarity.domain.model.SimilarityTypeDomain;
import com.calata.evaluator.similarity.domain.service.FingerprintFactory;
import com.calata.evaluator.similarity.domain.service.SimHashService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SimilarityAppService implements HandleEvaluationCompletedUseCase {

    private final FingerprintFactory fingerprintFactory;
    private final FingerprintWriter fingerprintWriter;
    private final FingerprintReader fingerprintReader;
    private final SimHashService simHashService;
    private final SimilarityResultWriter similarityResultWriter;
    private final DomainEventPublisher publisher;

    @Value("${similarity.sizeTolerance:0.20}")
    private double sizeTolerance;

    @Value("${similarity.recentLimit:5000}")
    private int recentLimit;

    @Value("${similarity.simhash.nearStrong:0.90}")
    private double nearStrong;

    public SimilarityAppService(FingerprintFactory fingerprintFactory,
            FingerprintWriter fingerprintWriter,
            FingerprintReader fingerprintReader,
            SimHashService simHashService,
            SimilarityResultWriter similarityResultWriter,
            DomainEventPublisher publisher) {
        this.fingerprintFactory = fingerprintFactory;
        this.fingerprintWriter = fingerprintWriter;
        this.fingerprintReader = fingerprintReader;
        this.simHashService = simHashService;
        this.similarityResultWriter = similarityResultWriter;
        this.publisher = publisher;
    }

    @Override
    public void handle(ProcessEvaluationCompletedCommand cmd) {
        Fingerprint fp = fingerprintFactory.create(
                cmd.submissionId(), cmd.userId(), cmd.language(), cmd.code(), cmd.completedAt());

        fingerprintWriter.upsert(fp);

        // Exact match
        var exact = fingerprintReader.findByShaRaw(fp.shaRaw())
                .filter(fingerprint -> !fingerprint.submissionId().equals(fp.submissionId()));
        if (exact.isPresent()) {
            SimilarityResult res = new SimilarityResult(
                    fp.submissionId(), fp.userId(), fp.language(), cmd.code(),
                    SimilarityTypeDomain.EXACT, 1.0, exact.get().submissionId(), Instant.now());
            persistAndPublish(res);
            return;
        }

        // Normalized match
        var norm = fingerprintReader.findByShaNorm(fp.shaNorm())
                .filter(doc -> !doc.submissionId().equals(fp.submissionId()));
        if (norm.isPresent()) {
            SimilarityResult res = new SimilarityResult(
                    fp.submissionId(), fp.userId(), fp.language(), cmd.code(),
                    SimilarityTypeDomain.NORMALIZED, 0.98, norm.get().submissionId(), Instant.now());
            persistAndPublish(res);
            return;
        }

        // Near match
        List<Fingerprint> candidates =
                fingerprintReader.findRecentByLangAndSize(fp.language(), fp.lineCount(), recentLimit, sizeTolerance);

        String bestId = null;
        double bestScore = 0.0;
        for (Fingerprint c : candidates) {
            if (c.submissionId().equals(fp.submissionId())) continue;
            double score = simHashService.score(fp.simhash64(), c.simhash64());
            if (score > bestScore) { bestScore = score; bestId = c.submissionId(); }
        }

        if (bestScore >= nearStrong) {
            SimilarityResult res = new SimilarityResult(
                    fp.submissionId(), fp.userId(), fp.language(), cmd.code(),
                    SimilarityTypeDomain.NEAR, bestScore, bestId, Instant.now());
            persistAndPublish(res);
        } else {
            SimilarityResult res = new SimilarityResult(
                    fp.submissionId(), fp.userId(), fp.language(), cmd.code(),
                    SimilarityTypeDomain.NONE, 0.0, null, Instant.now());
            persistAndPublish(res);
        }
    }

    private void persistAndPublish(SimilarityResult result) {
        similarityResultWriter.save(result);
        publisher.publishSimilarityComputed(result);
    }
}
