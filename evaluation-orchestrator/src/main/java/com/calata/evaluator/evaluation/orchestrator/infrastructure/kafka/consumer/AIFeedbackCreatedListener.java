package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.AIFeedbackCreated;
import com.calata.evaluator.contracts.events.SubmissionCreated;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessAIFeedbackCreatedCommand;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessCodeSubmissionCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleAIFeedbackCreatedUseCase;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleCodeSubmissionUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AIFeedbackCreatedListener {

    private final HandleAIFeedbackCreatedUseCase useCase;

    public AIFeedbackCreatedListener(HandleAIFeedbackCreatedUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.aiFeedbackCreated}", groupId = "submission-group")
    public void onMessage(AIFeedbackCreated msg){
        useCase.handle(new ProcessAIFeedbackCreatedCommand(
                msg.evaluationId(), msg.score(), msg.rubric(), msg.justification()
        ));
    }
}
