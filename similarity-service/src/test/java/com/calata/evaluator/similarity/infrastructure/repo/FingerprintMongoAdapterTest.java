package com.calata.evaluator.similarity.infrastructure.repo;

import com.calata.evaluator.similarity.domain.model.Fingerprint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FingerprintMongoAdapterTest {

    private SpringDataFingerprintRepository repo;
    private FingerprintMongoAdapter adapter;

    @BeforeEach
    void setUp() {
        repo = mock(SpringDataFingerprintRepository.class);
        adapter = new FingerprintMongoAdapter(repo);
    }

    @Test
    void upsert_savesMappedDocument() {
        // given
        Fingerprint fp = mock(Fingerprint.class);
        FingerprintDocument doc = mock(FingerprintDocument.class);

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.toDocument(fp)).thenReturn(doc);

            // when
            adapter.upsert(fp);

            // then
            verify(repo).save(doc);
        }
    }

    @Test
    void findByShaRaw_returnsMappedOptional_whenRepoReturnsDocument() {
        // given
        String shaRaw = "raw-sha";
        String userId = "user-1";

        FingerprintDocument doc = mock(FingerprintDocument.class);
        Fingerprint domainFp = mock(Fingerprint.class);

        when(repo.findFirstByShaRawAndUserIdNot(shaRaw, userId))
                .thenReturn(Optional.of(doc));

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.fingerprintToDomain(Optional.of(doc)))
                    .thenReturn(Optional.of(domainFp));

            // when
            Optional<Fingerprint> result = adapter.findByShaRaw(shaRaw, userId);

            // then
            assertTrue(result.isPresent());
            assertEquals(domainFp, result.get());
        }
    }

    @Test
    void findByShaRaw_returnsEmptyOptional_whenMapperReturnsEmpty() {
        // given
        String shaRaw = "raw-sha";
        String userId = "user-1";

        FingerprintDocument doc = mock(FingerprintDocument.class);
        when(repo.findFirstByShaRawAndUserIdNot(shaRaw, userId))
                .thenReturn(Optional.of(doc));

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.fingerprintToDomain(Optional.of(doc)))
                    .thenReturn(Optional.empty());

            // when
            Optional<Fingerprint> result = adapter.findByShaRaw(shaRaw, userId);

            // then
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void findByShaNorm_returnsMappedOptional_whenRepoReturnsDocument() {
        // given
        String shaNorm = "norm-sha";
        String userId = "user-1";

        FingerprintDocument doc = mock(FingerprintDocument.class);
        Fingerprint domainFp = mock(Fingerprint.class);

        when(repo.findFirstByShaNormAndUserIdNot(shaNorm, userId))
                .thenReturn(Optional.of(doc));

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.fingerprintToDomain(Optional.of(doc)))
                    .thenReturn(Optional.of(domainFp));

            // when
            Optional<Fingerprint> result = adapter.findByShaNorm(shaNorm, userId);

            // then
            assertTrue(result.isPresent());
            assertEquals(domainFp, result.get());
        }
    }

    @Test
    void findRecentByLangAndSize_returnsMappedList_whenSizeLessOrEqualLimit() {
        // given
        String lang = "java";
        int lineCount = 100;
        double tolerance = 0.1; // 10%
        String userId = "user-1";
        int limit = 5;

        int expectedMin = (int) Math.floor(lineCount * (1.0 - tolerance)); // 90
        int expectedMax = (int) Math.ceil(lineCount * (1.0 + tolerance));  // 110

        FingerprintDocument doc1 = mock(FingerprintDocument.class);
        FingerprintDocument doc2 = mock(FingerprintDocument.class);
        List<FingerprintDocument> docs = List.of(doc1, doc2);

        when(repo.findByLangAndCountBetween(
                eq(lang),
                eq(expectedMin),
                eq(expectedMax),
                eq(userId),
                any(Sort.class))
        ).thenReturn(docs);

        Fingerprint fp1 = mock(Fingerprint.class);
        Fingerprint fp2 = mock(Fingerprint.class);
        List<Fingerprint> mapped = List.of(fp1, fp2);

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.fingerprintsToDomain(docs))
                    .thenReturn(mapped);

            // when
            List<Fingerprint> result = adapter.findRecentByLangAndSize(
                    lang, lineCount, limit, tolerance, userId
            );

            // then
            assertEquals(mapped, result);

            // verificamos el Sort usado
            ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
            verify(repo).findByLangAndCountBetween(
                    eq(lang),
                    eq(expectedMin),
                    eq(expectedMax),
                    eq(userId),
                    sortCaptor.capture()
            );

            Sort sort = sortCaptor.getValue();
            assertNotNull(sort);
            Sort.Order order = sort.getOrderFor("createdAt");
            assertNotNull(order);
            assertEquals(Sort.Direction.DESC, order.getDirection());
        }
    }

    @Test
    void findRecentByLangAndSize_truncatesToLimit_whenMoreResultsThanLimit() {
        // given
        String lang = "java";
        int lineCount = 100;
        double tolerance = 0.2;
        String userId = "user-1";
        int limit = 2;

        int expectedMin = (int) Math.floor(lineCount * (1.0 - tolerance));
        int expectedMax = (int) Math.ceil(lineCount * (1.0 + tolerance));

        List<FingerprintDocument> docs = List.of(
                mock(FingerprintDocument.class),
                mock(FingerprintDocument.class),
                mock(FingerprintDocument.class)
        );

        when(repo.findByLangAndCountBetween(
                eq(lang),
                eq(expectedMin),
                eq(expectedMax),
                eq(userId),
                any(Sort.class))
        ).thenReturn(docs);

        Fingerprint fp1 = mock(Fingerprint.class);
        Fingerprint fp2 = mock(Fingerprint.class);
        Fingerprint fp3 = mock(Fingerprint.class);
        List<Fingerprint> mapped = List.of(fp1, fp2, fp3);

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.fingerprintsToDomain(docs))
                    .thenReturn(mapped);

            // when
            List<Fingerprint> result = adapter.findRecentByLangAndSize(
                    lang, lineCount, limit, tolerance, userId
            );

            // then: se recorta a 'limit'
            assertEquals(limit, result.size());
            assertEquals(fp1, result.get(0));
            assertEquals(fp2, result.get(1));
        }
    }
}
