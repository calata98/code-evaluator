package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.contracts.dto.SubmissionResponse;
import com.calata.evaluator.contracts.dto.UpdateSubmissionStatus;
import com.calata.evaluator.submission.api.application.command.CreateSubmissionCommand;
import com.calata.evaluator.submission.api.application.command.GetSubmissionCommand;
import com.calata.evaluator.submission.api.application.command.UpdateSubmissionStatusCommand;
import com.calata.evaluator.submission.api.application.port.in.CreateSubmissionUseCase;
import com.calata.evaluator.submission.api.application.port.in.GetSubmissionUseCase;
import com.calata.evaluator.submission.api.application.port.in.UpdateSubmissionStatusUseCase;
import com.calata.evaluator.contracts.dto.SubmissionCodeResponse;
import com.calata.evaluator.submission.api.infrastructure.web.dto.SubmissionIdResponse;
import com.calata.evaluator.submission.api.infrastructure.web.dto.SubmissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
    @RequestMapping("/submissions")
    @RequiredArgsConstructor
    class SubmissionController {

        private final CreateSubmissionUseCase createSubmission;
        private final UpdateSubmissionStatusUseCase updateSubmissionStatus;
        private final GetSubmissionUseCase getSubmission;

        @GetMapping("/{id}")
        public ResponseEntity<SubmissionResponse> getById(@PathVariable String id) {
            var cmd = new GetSubmissionCommand(id);
            var submission = getSubmission.getSubmission(cmd.submissionId());
            return ResponseEntity.ok(submission);
        }

        @GetMapping("/mine")
        public ResponseEntity<List<SubmissionResponse>> getByUserId(@AuthenticationPrincipal Jwt jwt) {
            String userId = jwt.getSubject();
            var submission = getSubmission.getSubmissionByUserId(userId);
            return ResponseEntity.ok(submission);
        }

        @PostMapping
        public ResponseEntity<SubmissionIdResponse> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubmissionRequest req) {
            String userId = jwt.getSubject();
            var submissionCommand = new CreateSubmissionCommand(
                    userId, req.title(), req.code(), req.language()
            );
            var id = createSubmission.handle(submissionCommand);
            return ResponseEntity.accepted().body(new SubmissionIdResponse(id));
        }

        @PutMapping("/status")
        public ResponseEntity<SubmissionIdResponse> updateStatus(@RequestBody UpdateSubmissionStatus req) {
            var cmd = new UpdateSubmissionStatusCommand(
                    req.submissionId(), req.status()
            );
            var id = updateSubmissionStatus.updateSubmissionStatus(cmd.submissionId(), cmd.status());
            return ResponseEntity.accepted().body(new SubmissionIdResponse(id));
        }

        @GetMapping("/{id}/code")
        public ResponseEntity<SubmissionCodeResponse> getSubmissionCode(@PathVariable String id) {
            var submission = getSubmission.getSubmission(id);

            if (submission == null) {
                return ResponseEntity.notFound().build();
            }

            var dto = new SubmissionCodeResponse(
                    submission.id(),
                    submission.code()
            );

            return ResponseEntity.ok(dto);
        }
    }
