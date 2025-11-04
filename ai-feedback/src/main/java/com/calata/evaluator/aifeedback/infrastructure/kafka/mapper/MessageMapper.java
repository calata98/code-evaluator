package com.calata.evaluator.aifeedback.infrastructure.kafka.mapper;

import com.calata.evaluator.aifeedback.application.command.ProcessAIFeedbackRequestedCommand;
import com.calata.evaluator.contracts.events.AIFeedbackRequested;

public final class MessageMapper {
    private MessageMapper(){}
    public static ProcessAIFeedbackRequestedCommand toCommand(AIFeedbackRequested m) {
        return new ProcessAIFeedbackRequestedCommand(
                m.evaluationId(), m.submissionId(), m.language(), m.code()
        );
    }
}
