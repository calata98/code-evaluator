package com.calata.evaluator.similarity.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimHashServiceTest {

    private SimHashService service;

    @BeforeEach
    void setUp() {
        service = new SimHashService();
    }

    @Test
    void score_returns1_whenHashesAreIdentical() {
        // given
        long h = 0b1010L;

        // when
        double score = service.score(h, h);

        // then
        assertEquals(1.0, score, 1e-9);
    }

    @Test
    void score_returns0_whenAllBitsDiffer() {
        long a = -1L;
        long b = 0L;

        // when
        double score = service.score(a, b);

        // then
        assertEquals(0.0, score, 1e-9);
    }

    @Test
    void score_returns05_whenHalfOfBitsDiffer() {

        long a = 0xFFFFFFFF00000000L;
        long b = 0xFFFFFFFFFFFFFFFFL;

        // when
        double score = service.score(a, b);

        // then
        assertEquals(0.5, score, 1e-9);
    }

    @Test
    void score_handlesZeroAndNonZero() {
        // given
        long a = 0L;
        long b = 0b1111L;

        // when
        double score = service.score(a, b);

        assertEquals(60.0 / 64.0, score, 1e-9);
    }

    @Test
    void score_isSymmetric() {
        // given
        long a = 0x0F0F0F0F0F0F0F0FL;
        long b = 0x00FF00FF00FF00FFL;

        // when
        double scoreAB = service.score(a, b);
        double scoreBA = service.score(b, a);

        // then
        assertEquals(scoreAB, scoreBA, 1e-12);
    }
}
