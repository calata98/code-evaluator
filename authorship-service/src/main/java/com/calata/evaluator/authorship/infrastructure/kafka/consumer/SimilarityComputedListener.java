package com.calata.evaluator.authorship.infrastructure.kafka.consumer;

import com.calata.evaluator.authorship.application.command.ProcessSimilarityComputedCommand;
import com.calata.evaluator.authorship.application.port.in.HandleSimilarityComputedUseCase;
import com.calata.evaluator.contracts.events.SimilarityComputed;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SimilarityComputedListener {

    private final HandleSimilarityComputedUseCase useCase;

    public SimilarityComputedListener(HandleSimilarityComputedUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.similarityComputed}", groupId = "authorship-group")
    public void onMessage(SimilarityComputed evt) {
        var cmd = new ProcessSimilarityComputedCommand(
                evt.submissionId(), evt.userId(), evt.language(), evt.type().name(), evt.score(),
                evt.matchedSubmissionId(), evt.createdAt(), evt.code()
        );
        useCase.handle(cmd);
    }
}
