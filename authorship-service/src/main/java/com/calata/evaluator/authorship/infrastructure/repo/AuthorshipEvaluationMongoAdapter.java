package com.calata.evaluator.authorship.infrastructure.repo;

import com.calata.evaluator.authorship.application.port.out.AuthorshipEvaluationReader;
import com.calata.evaluator.authorship.application.port.out.AuthorshipEvaluationWriter;
import com.calata.evaluator.authorship.domain.model.AuthorshipEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthorshipEvaluationMongoAdapter implements AuthorshipEvaluationWriter, AuthorshipEvaluationReader {

    private final SpringDataAuthorshipEvaluationRepository repo;

    @Override
    public void save(AuthorshipEvaluation authorshipEvaluation) {
        repo.save(Mappers.toDocument(authorshipEvaluation)).blockOptional();
    }

    @Override
    public Mono<AuthorshipEvaluation> findBySubmissionId(String id) {
        return repo.findBySubmissionId(id).map(Mappers::toDomain);
    }
}
