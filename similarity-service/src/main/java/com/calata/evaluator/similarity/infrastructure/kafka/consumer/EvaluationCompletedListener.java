package com.calata.evaluator.similarity.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.EvaluationCreated;
import com.calata.evaluator.similarity.application.command.ProcessEvaluationCompletedCommand;
import com.calata.evaluator.similarity.application.port.in.HandleEvaluationCompletedUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EvaluationCompletedListener {

    private final HandleEvaluationCompletedUseCase useCase;

    public EvaluationCompletedListener(HandleEvaluationCompletedUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.evaluationCreated}", groupId = "evaluation-completed-group")
    public void onMessage(EvaluationCreated evt) {
        var cmd = new ProcessEvaluationCompletedCommand(
                evt.submissionId(), evt.userId(), evt.language(), evt.code(), evt.createdAt());
        useCase.handle(cmd);
    }
}
