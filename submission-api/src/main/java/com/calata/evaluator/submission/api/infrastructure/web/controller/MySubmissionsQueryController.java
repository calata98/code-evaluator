package com.calata.evaluator.submission.api.infrastructure.web.controller;

import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewDocument;
import com.calata.evaluator.submission.api.infrastructure.repo.SubmissionDetailViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MySubmissionsQueryController {

    private final SubmissionDetailViewRepository repo;

    @GetMapping("/submissions")
    public List<SubmissionDetailViewDocument> listMine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var userId = jwt.getSubject();
        return repo.findBySubmissionUserIdOrderBySubmissionCreatedAtDesc(
                userId, PageRequest.of(page, size));
    }

    @GetMapping("/submissions/{id}")
    public SubmissionDetailViewDocument getOne(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {
        var view = repo.findById(id).orElseThrow();
        if (!userId(jwt).equals(view.submission().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return view;
    }

    private String userId(Jwt jwt) { return jwt.getSubject(); }
}
