package com.calata.evaluator.authorship.infrastructure.submission;

import com.calata.evaluator.authorship.application.port.out.LoadSubmissionCode;
import com.calata.evaluator.contracts.dto.SubmissionCodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SubmissionCodeRestAdapter implements LoadSubmissionCode {

    @Value("${INTERNAL_API_KEY}")
    String apiKey;

    private final WebClient submissionWebClient;

    public SubmissionCodeRestAdapter(WebClient submissionWebClient) {
        this.submissionWebClient = submissionWebClient;
    }

    @Override
    public SubmissionCodeResponse loadById(String submissionId) {
        var dto = submissionWebClient
                .get()
                .uri("/submissions/{id}/code", submissionId)
                .header("X-Internal-Api-Key", apiKey)
                .retrieve()
                .bodyToMono(SubmissionCodeResponse.class)
                .block();

        if (dto == null) {
            throw new IllegalStateException("Submission not found: " + submissionId);
        }

        return new SubmissionCodeResponse(dto.id(), dto.code());
    }
}
