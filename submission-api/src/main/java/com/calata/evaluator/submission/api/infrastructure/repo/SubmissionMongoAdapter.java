package com.calata.evaluator.submission.api.infrastructure.repo;

import com.calata.evaluator.submission.api.application.port.out.SubmissionReader;
import com.calata.evaluator.submission.api.application.port.out.SubmissionWriter;
import com.calata.evaluator.submission.api.domain.model.Submission;
import com.calata.evaluator.submission.api.domain.model.SubmissionStatus;
import com.calata.evaluator.submission.api.infrastructure.repo.mapper.SubmissionPersistenceMapper;
import com.mongodb.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionMongoAdapter implements SubmissionWriter, SubmissionReader {

    private final SpringDataSubmissionRepository repo;

    public SubmissionMongoAdapter(SpringDataSubmissionRepository repo) {
        this.repo = repo;
    }

    @Override
    public Submission save(Submission submission) {
        var existing = repo.findById(submission.getId());
        if (existing.isPresent()) {
            return SubmissionPersistenceMapper.toDomain(existing.get());
        }
        try {
            SubmissionDocument toSave = SubmissionPersistenceMapper.toDocument(submission);
            SubmissionDocument saved = repo.save(toSave);
            return SubmissionPersistenceMapper.toDomain(saved);
        } catch (DuplicateKeyException e) {
            return repo.findById(submission.getId())
                    .map(SubmissionPersistenceMapper::toDomain)
                    .orElseThrow(() -> e);
        }
    }

    @Override
    public Submission updateStatus(String submissionId, String status) {
        var existing = repo.findById(submissionId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Submission with ID " + submissionId + " not found.");
        }
        var submissionDoc = existing.get();
        submissionDoc.setStatus(SubmissionStatus.valueOf(status));
        var updated = repo.save(submissionDoc);
        return SubmissionPersistenceMapper.toDomain(updated);
    }

    @Override
    public Submission getById(String submissionId) {
        var existing = repo.findById(submissionId);

        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Submission with ID " + submissionId + " not found.");
        }

        return SubmissionPersistenceMapper.toDomain(existing.get());
    }

    @Override
    public List<Submission> getByUserId(String userId) {
        var documents = repo.findByUserId(userId);
        return documents.stream()
                .map(SubmissionPersistenceMapper::toDomain)
                .toList();
    }
}
