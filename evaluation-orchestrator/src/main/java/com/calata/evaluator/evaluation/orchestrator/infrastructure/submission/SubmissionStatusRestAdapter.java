package com.calata.evaluator.evaluation.orchestrator.infrastructure.submission;

import com.calata.evaluator.contracts.dto.SubmissionResponse;
import com.calata.evaluator.contracts.dto.UpdateSubmissionStatus;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.SubmissionReader;
import com.calata.evaluator.evaluation.orchestrator.application.port.out.SubmissionStatusUpdater;
import com.calata.evaluator.evaluation.orchestrator.infrastructure.config.SubmissionApiProps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SubmissionStatusRestAdapter implements SubmissionStatusUpdater, SubmissionReader {

    private final WebClient webClient;

    @Value("${INTERNAL_API_KEY}")
    String apiKey;

    public SubmissionStatusRestAdapter(WebClient.Builder builder, SubmissionApiProps props) {
        this.webClient = builder.baseUrl(props.baseUrl()).build();
    }

    @Override
    public void markRunning(String submissionId) {
        var body = new UpdateSubmissionStatus(submissionId, "RUNNING");
        webClient.put()
                .uri("/submissions/status")
                .header("X-Internal-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public SubmissionSnapshot findById(String submissionId) {
        var dto = webClient.get()
                .uri("/submissions/{id}", submissionId)
                .header("X-Internal-Api-Key", apiKey)
                .retrieve()
                .bodyToMono(SubmissionResponse.class)
                .block();

        if (dto == null) throw new IllegalStateException("Submission not found: " + submissionId);

        return new SubmissionSnapshot(
                dto.id(),
                dto.language(),
                dto.code(),
                dto.userId()
        );
    }
}
