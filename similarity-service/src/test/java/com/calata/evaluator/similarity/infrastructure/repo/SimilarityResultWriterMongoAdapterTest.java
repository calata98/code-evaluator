package com.calata.evaluator.similarity.infrastructure.repo;


import com.calata.evaluator.similarity.domain.model.SimilarityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class SimilarityResultWriterMongoAdapterTest {

    private SpringDataSimilarityResultRepository repo;
    private SimilarityResultWriterMongoAdapter adapter;

    @BeforeEach
    void setUp() {
        repo = mock(SpringDataSimilarityResultRepository.class);
        adapter = new SimilarityResultWriterMongoAdapter(repo);
    }

    @Test
    void save_delegatesToMapperAndRepository() {
        // given
        SimilarityResult domainResult = mock(SimilarityResult.class);
        SimilarityResultDocument doc = mock(SimilarityResultDocument.class);

        try (MockedStatic<Mappers> mappers = mockStatic(Mappers.class)) {
            mappers.when(() -> Mappers.toDocument(domainResult))
                    .thenReturn(doc);

            // when
            adapter.save(domainResult);

            // then
            verify(repo).save(doc);
        }
    }
}
