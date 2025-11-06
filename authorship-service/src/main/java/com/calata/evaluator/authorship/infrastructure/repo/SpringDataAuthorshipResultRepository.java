package com.calata.evaluator.authorship.infrastructure.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataAuthorshipResultRepository extends MongoRepository<AuthorshipResultDocument, String> { }

