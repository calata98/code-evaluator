package com.calata.evaluator.similarity.application.service;

import com.calata.evaluator.similarity.application.command.ProcessEvaluationCompletedCommand;
import com.calata.evaluator.similarity.application.port.out.DomainEventPublisher;
import com.calata.evaluator.similarity.application.port.out.FingerprintReader;
import com.calata.evaluator.similarity.application.port.out.FingerprintWriter;
import com.calata.evaluator.similarity.application.port.out.SimilarityResultWriter;
import com.calata.evaluator.similarity.domain.model.Fingerprint;
import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import com.calata.evaluator.similarity.domain.model.SimilarityTypeDomain;
import com.calata.evaluator.similarity.domain.service.FingerprintFactory;
import com.calata.evaluator.similarity.domain.service.SimHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimilarityAppServiceTest {

    @Mock
    private FingerprintFactory fingerprintFactory;

    @Mock
    private FingerprintWriter fingerprintWriter;

    @Mock
    private FingerprintReader fingerprintReader;

    @Mock
    private SimHashService simHashService;

    @Mock
    private SimilarityResultWriter similarityResultWriter;

    @Mock
    private DomainEventPublisher publisher;

    @Mock
    private ProcessEvaluationCompletedCommand cmd;

    @Mock
    private Fingerprint fp;

    @Captor
    private ArgumentCaptor<SimilarityResult> similarityResultCaptor;

    private SimilarityAppService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SimilarityAppService(
                fingerprintFactory,
                fingerprintWriter,
                fingerprintReader,
                simHashService,
                similarityResultWriter,
                publisher
        );

        // Inyectamos los @Value a mano
        setField(service, "sizeTolerance", 0.20d);
        setField(service, "recentLimit", 100);
        setField(service, "nearStrong", 0.90d);

        // Stubs comunes usados en TODOS los tests
        when(cmd.submissionId()).thenReturn("sub-1");
        when(cmd.userId()).thenReturn("user-1");
        when(cmd.language()).thenReturn("java");
        when(cmd.code()).thenReturn("System.out.println();");
        when(cmd.completedAt()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));

        when(fingerprintFactory.create(
                anyString(), anyString(), anyString(), anyString(), any(Instant.class))
        ).thenReturn(fp);

        when(fp.submissionId()).thenReturn("sub-1");
        when(fp.userId()).thenReturn("user-1");
        when(fp.language()).thenReturn("java");
        when(fp.shaRaw()).thenReturn("sha-raw");
        when(fp.shaNorm()).thenReturn("sha-norm");
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = SimilarityAppService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ---------------------------------------------------------------------
    // EXACT MATCH
    // ---------------------------------------------------------------------

    @Test
    void handle_whenExactMatch_shouldPersistExactAndReturn() {
        Fingerprint other = mock(Fingerprint.class);
        when(other.submissionId()).thenReturn("sub-2");

        when(fingerprintReader.findByShaRaw("sha-raw", "user-1"))
                .thenReturn(Optional.of(other));

        service.handle(cmd);

        verify(fingerprintWriter).upsert(fp);
        verify(fingerprintReader).findByShaRaw("sha-raw", "user-1");
        verify(fingerprintReader, never()).findByShaNorm(anyString(), anyString());
        verify(fingerprintReader, never()).findRecentByLangAndSize(anyString(), anyInt(), anyInt(), anyDouble(), anyString());
        verifyNoInteractions(simHashService);

        verify(similarityResultWriter).save(similarityResultCaptor.capture());
        SimilarityResult res = similarityResultCaptor.getValue();

        assertThat(res.type()).isEqualTo(SimilarityTypeDomain.EXACT);
        assertThat(res.score()).isEqualTo(1.0);
        assertThat(res.submissionId()).isEqualTo("sub-1");
        assertThat(res.matchedSubmissionId()).isEqualTo("sub-2");

        verify(publisher).publishSimilarityComputed(res);
    }

    // ---------------------------------------------------------------------
    // NORMALIZED MATCH
    // ---------------------------------------------------------------------

    @Test
    void handle_whenNormalizedMatch_shouldPersistNormalizedAndReturn() {
        when(fingerprintReader.findByShaRaw("sha-raw", "user-1"))
                .thenReturn(Optional.empty());

        Fingerprint other = mock(Fingerprint.class);
        when(other.submissionId()).thenReturn("sub-3");

        when(fingerprintReader.findByShaNorm("sha-norm", "user-1"))
                .thenReturn(Optional.of(other));

        service.handle(cmd);

        verify(fingerprintWriter).upsert(fp);
        verify(fingerprintReader).findByShaRaw("sha-raw", "user-1");
        verify(fingerprintReader).findByShaNorm("sha-norm", "user-1");

        verify(similarityResultWriter).save(similarityResultCaptor.capture());
        SimilarityResult res = similarityResultCaptor.getValue();

        assertThat(res.type()).isEqualTo(SimilarityTypeDomain.NORMALIZED);
        assertThat(res.score()).isEqualTo(0.98);
        assertThat(res.submissionId()).isEqualTo("sub-1");
        assertThat(res.matchedSubmissionId()).isEqualTo("sub-3");

        verify(publisher).publishSimilarityComputed(res);
        verifyNoInteractions(simHashService);
    }

    // ---------------------------------------------------------------------
    // NEAR MATCH (score ≥ nearStrong)
    // ---------------------------------------------------------------------

    @Test
    void handle_whenNearStrongMatch_shouldPersistNearResult() {
        when(fingerprintReader.findByShaRaw("sha-raw", "user-1"))
                .thenReturn(Optional.empty());
        when(fingerprintReader.findByShaNorm("sha-norm", "user-1"))
                .thenReturn(Optional.empty());

        when(fp.lineCount()).thenReturn(10);
        when(fp.simhash64()).thenReturn(123L);

        Fingerprint candidate = mock(Fingerprint.class);
        when(candidate.submissionId()).thenReturn("sub-4");
        when(candidate.simhash64()).thenReturn(222L);

        when(fingerprintReader.findRecentByLangAndSize(
                eq("java"), eq(10), eq(100), eq(0.20d), eq("user-1")
        )).thenReturn(List.of(candidate));

        when(simHashService.score(123L, 222L)).thenReturn(0.95); // ≥ nearStrong

        service.handle(cmd);

        verify(fingerprintWriter).upsert(fp);

        verify(similarityResultWriter).save(similarityResultCaptor.capture());
        SimilarityResult res = similarityResultCaptor.getValue();

        assertThat(res.type()).isEqualTo(SimilarityTypeDomain.NEAR);
        assertThat(res.score()).isEqualTo(0.95);
        assertThat(res.submissionId()).isEqualTo("sub-1");
        assertThat(res.matchedSubmissionId()).isEqualTo("sub-4");

        verify(publisher).publishSimilarityComputed(res);
    }

    // ---------------------------------------------------------------------
    // NONE (no score suficientemente alto)
    // ---------------------------------------------------------------------

    @Test
    void handle_whenNoStrongMatch_shouldPersistNoneResult() {
        when(fingerprintReader.findByShaRaw("sha-raw", "user-1"))
                .thenReturn(Optional.empty());
        when(fingerprintReader.findByShaNorm("sha-norm", "user-1"))
                .thenReturn(Optional.empty());

        when(fp.lineCount()).thenReturn(10);
        when(fp.simhash64()).thenReturn(123L);

        Fingerprint candidate = mock(Fingerprint.class);
        when(candidate.submissionId()).thenReturn("sub-6");
        when(candidate.simhash64()).thenReturn(222L);

        when(fingerprintReader.findRecentByLangAndSize(
                eq("java"), eq(10), eq(100), eq(0.20d), eq("user-1")
        )).thenReturn(List.of(candidate));

        when(simHashService.score(123L, 222L)).thenReturn(0.7); // < nearStrong

        service.handle(cmd);

        verify(fingerprintWriter).upsert(fp);

        verify(similarityResultWriter).save(similarityResultCaptor.capture());
        SimilarityResult res = similarityResultCaptor.getValue();

        assertThat(res.type()).isEqualTo(SimilarityTypeDomain.NONE);
        assertThat(res.score()).isEqualTo(0.0);
        assertThat(res.submissionId()).isEqualTo("sub-1");
        assertThat(res.matchedSubmissionId()).isNull();

        verify(publisher).publishSimilarityComputed(res);
    }
}
