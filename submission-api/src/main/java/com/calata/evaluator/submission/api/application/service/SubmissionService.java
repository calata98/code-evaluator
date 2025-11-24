package com.calata.evaluator.submission.api.application.service;

import com.calata.evaluator.submission.api.application.command.CreateSubmissionCommand;
import com.calata.evaluator.submission.api.application.port.in.CreateSubmissionUseCase;
import com.calata.evaluator.submission.api.application.port.in.GetSubmissionUseCase;
import com.calata.evaluator.submission.api.application.port.in.UpdateSubmissionStatusUseCase;
import com.calata.evaluator.submission.api.application.port.out.SubmissionEventsPublisher;
import com.calata.evaluator.submission.api.application.port.out.SubmissionReader;
import com.calata.evaluator.submission.api.application.port.out.SubmissionWriter;
import com.calata.evaluator.submission.api.domain.model.submission.Submission;
import com.calata.evaluator.contracts.dto.SubmissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService implements CreateSubmissionUseCase, UpdateSubmissionStatusUseCase, GetSubmissionUseCase {

    private final SubmissionWriter submissionWriter;
    private final SubmissionEventsPublisher events;
    private final SubmissionReader submissionReader;

    @Override
    public String handle(CreateSubmissionCommand c){
        var submission = Submission.create(c.userId(), c.title(), c.code(), c.language());

        events.publishSubmission(submission);
        submissionWriter.save(submission);

        return submission.getId();
    }

    @Override
    public String updateSubmissionStatus(String submissionId, String status) {
       var submissionUpdated = submissionWriter.updateStatus(submissionId, status);
       events.publishSubmissionStatusUpdated(submissionUpdated);

       return submissionUpdated.getId();
    }

    @Override
    public SubmissionResponse getSubmission(String submissionId) {
       var submission = submissionReader.getById(submissionId);
       return toDto(submission);
    }

    @Override
    public List<SubmissionResponse> getSubmissionByUserId(String userId) {
        var submissions = submissionReader.getByUserId(userId);
        return submissions.stream().map(this::toDto).toList();
    }

    private SubmissionResponse toDto(Submission submission){
        return new SubmissionResponse(
                submission.getId(),
                submission.getTitle(),
                submission.getLanguage().name(),
                submission.getCode(),
                submission.getStatus().name(),
                submission.getCreatedAt().toString(),
                submission.getUserId()
        );
    }
}
