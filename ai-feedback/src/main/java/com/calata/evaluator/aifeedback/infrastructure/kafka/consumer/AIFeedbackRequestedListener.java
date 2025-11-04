package com.calata.evaluator.aifeedback.infrastructure.kafka.consumer;

import com.calata.evaluator.aifeedback.application.command.ProcessAIFeedbackRequestedCommand;
import com.calata.evaluator.aifeedback.application.port.in.HandleAIFeedbackRequestedUseCase;
import com.calata.evaluator.contracts.events.AIFeedbackRequested;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AIFeedbackRequestedListener {

    private final HandleAIFeedbackRequestedUseCase useCase;

    public AIFeedbackRequestedListener(HandleAIFeedbackRequestedUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.feedbackRequested}", groupId = "ai-feedback-group")
    public void onMessage(AIFeedbackRequested msg){
        System.out.println("Received AI Feedback Requested event for Evaluation ID: " + msg.evaluationId());
        useCase.handle(new ProcessAIFeedbackRequestedCommand(
                msg.evaluationId(), msg.submissionId(), msg.language(), msg.code()));
    }
}
