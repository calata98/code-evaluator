package com.calata.evaluator.similarity.domain.service;

import org.springframework.stereotype.Component;

@Component
public class SimHashService {

    public double score(long simhashA, long simhashB) {
        int dist = Long.bitCount(simhashA ^ simhashB);
        return 1.0 - (dist / 64.0);
    }
}
