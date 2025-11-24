package com.calata.evaluator.aifeedback.application.service;

import com.calata.evaluator.aifeedback.application.command.ProcessAIFeedbackRequestedCommand;
import com.calata.evaluator.aifeedback.application.port.in.HandleAIFeedbackRequestedUseCase;
import com.calata.evaluator.aifeedback.application.port.out.FeedbackCreatedPublisher;
import com.calata.evaluator.aifeedback.application.port.out.FeedbackGenerator;
import com.calata.evaluator.aifeedback.application.port.out.FeedbackWriter;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.aifeedback.domain.service.FeedbackSynthesisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AIFeedbackAppService implements HandleAIFeedbackRequestedUseCase {

    private final FeedbackGenerator generator;
    private final FeedbackWriter writer;
    private final FeedbackCreatedPublisher publisher;
    private final FeedbackSynthesisService synthesis;

    public AIFeedbackAppService(FeedbackGenerator generator,
            FeedbackWriter writer,
            FeedbackCreatedPublisher publisher,
            FeedbackSynthesisService synthesis) {
        this.generator = generator;
        this.writer = writer;
        this.publisher = publisher;
        this.synthesis = synthesis;
    }

    @Override
    @Transactional
    public void handle(ProcessAIFeedbackRequestedCommand cmd) {
        if (writer.existsForEvaluation(cmd.evaluationId())) return;

        var raw = generator.generateWithScore(cmd.language(), cmd.code(), cmd.stderr(), cmd.stdout(), cmd.timeMs(), cmd.memoryMb());
        List<Feedback> validated = synthesis.validateAndNormalize(raw.items());

        var saved = writer.saveAll(cmd.evaluationId(), validated);
        publisher.publish(cmd.evaluationId(), cmd.submissionId(), saved, raw.score(), raw.rubric(), raw.justification());
    }
}
