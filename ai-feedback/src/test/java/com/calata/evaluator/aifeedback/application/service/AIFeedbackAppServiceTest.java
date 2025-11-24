package com.calata.evaluator.aifeedback.application.service;

import com.calata.evaluator.aifeedback.application.command.ProcessAIFeedbackRequestedCommand;
import com.calata.evaluator.aifeedback.application.port.out.FeedbackCreatedPublisher;
import com.calata.evaluator.aifeedback.application.port.out.FeedbackGenerator;
import com.calata.evaluator.aifeedback.application.port.out.FeedbackWriter;
import com.calata.evaluator.aifeedback.domain.model.Feedback;
import com.calata.evaluator.aifeedback.domain.model.FeedbackAggregate;
import com.calata.evaluator.aifeedback.domain.service.FeedbackSynthesisService;
import com.calata.evaluator.contracts.types.FeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIFeedbackAppServiceTest {

    @Mock
    private FeedbackGenerator generator;

    @Mock
    private FeedbackWriter writer;

    @Mock
    private FeedbackCreatedPublisher publisher;

    @Mock
    private FeedbackSynthesisService synthesis;

    @InjectMocks
    private AIFeedbackAppService service;

    @Test
    void handle_shouldReturnImmediatelyWhenFeedbackAlreadyExists() {
        // given
        String evaluationId = "eval-123";

        ProcessAIFeedbackRequestedCommand cmd = mock(ProcessAIFeedbackRequestedCommand.class);
        when(cmd.evaluationId()).thenReturn(evaluationId);

        when(writer.existsForEvaluation(evaluationId)).thenReturn(true);

        // when
        service.handle(cmd);

        // then
        verify(writer).existsForEvaluation(evaluationId);

        verifyNoInteractions(generator, synthesis, publisher);
        verify(writer, never()).saveAll(anyString(), anyList());
    }

    @Test
    void handle_shouldGenerateNormalizePersistAndPublishWhenFeedbackNotExists() {
        // given
        String evaluationId = "eval-123";
        String submissionId = "sub-123";
        String language = "java";
        String code = "public class Main {}";
        String stderr = "";
        String stdout = "OK";
        long timeMs = 150L;
        long memoryMb = 64L;

        ProcessAIFeedbackRequestedCommand cmd = mock(ProcessAIFeedbackRequestedCommand.class);
        when(cmd.evaluationId()).thenReturn(evaluationId);
        when(cmd.submissionId()).thenReturn(submissionId);
        when(cmd.language()).thenReturn(language);
        when(cmd.code()).thenReturn(code);
        when(cmd.stderr()).thenReturn(stderr);
        when(cmd.stdout()).thenReturn(stdout);
        when(cmd.timeMs()).thenReturn(timeMs);
        when(cmd.memoryMb()).thenReturn(memoryMb);

        when(writer.existsForEvaluation(evaluationId)).thenReturn(false);

        Feedback f1 = mock(Feedback.class);
        Feedback f2 = mock(Feedback.class);
        List<Feedback> rawItems = List.of(f1, f2);

        int score = 87;
        Map<FeedbackType, Integer> rubric = Map.of();
        String justification = "Example justification";

        FeedbackAggregate rawResult = new FeedbackAggregate(score, rubric, justification, rawItems);


        when(generator.generateWithScore(language, code, stderr, stdout, timeMs, memoryMb))
                .thenReturn(rawResult);

        // List normalized
        Feedback normalized1 = mock(Feedback.class);
        Feedback normalized2 = mock(Feedback.class);
        List<Feedback> normalizedList = List.of(normalized1, normalized2);

        when(synthesis.validateAndNormalize(rawItems)).thenReturn(normalizedList);

        Feedback saved1 = mock(Feedback.class);
        Feedback saved2 = mock(Feedback.class);
        List<Feedback> savedList = List.of(saved1, saved2);

        when(writer.saveAll(evaluationId, normalizedList)).thenReturn(savedList);

        // when
        service.handle(cmd);

        // then
        verify(writer).existsForEvaluation(evaluationId);
        verify(generator).generateWithScore(language, code, stderr, stdout, timeMs, memoryMb);
        verify(synthesis).validateAndNormalize(rawItems);
        verify(writer).saveAll(evaluationId, normalizedList);
        verify(publisher).publish(evaluationId, submissionId, savedList, score, rubric, justification);
        verifyNoMoreInteractions(generator, writer, publisher, synthesis);
    }

    @Test
    void handle_shouldWorkWhenGeneratorReturnsEmptyList() {
        // given
        String evaluationId = "eval-empty";
        String submissionId = "sub-empty";

        String language = "java";
        String code = "class X {}";
        String stderr = "";
        String stdout = "";
        long timeMs = 10L;
        long memoryMb = 16L;

        ProcessAIFeedbackRequestedCommand cmd = mock(ProcessAIFeedbackRequestedCommand.class);
        when(cmd.evaluationId()).thenReturn(evaluationId);
        when(cmd.submissionId()).thenReturn(submissionId);
        when(cmd.language()).thenReturn(language);
        when(cmd.code()).thenReturn(code);
        when(cmd.stderr()).thenReturn(stderr);
        when(cmd.stdout()).thenReturn(stdout);
        when(cmd.timeMs()).thenReturn(timeMs);
        when(cmd.memoryMb()).thenReturn(memoryMb);

        when(writer.existsForEvaluation(evaluationId)).thenReturn(false);

        List<Feedback> emptyItems = List.of();
        int score = 0;
        Map<FeedbackType, Integer> rubric = Map.of();
        String justification = "Sin problemas";

        FeedbackAggregate rawResult = new FeedbackAggregate(score, rubric, justification, emptyItems);

        when(generator.generateWithScore(language, code, stderr, stdout, timeMs, memoryMb))
                .thenReturn(rawResult);

        when(synthesis.validateAndNormalize(emptyItems)).thenReturn(emptyItems);
        when(writer.saveAll(evaluationId, emptyItems)).thenReturn(emptyItems);

        // when
        service.handle(cmd);

        // then
        verify(generator).generateWithScore(language, code, stderr, stdout, timeMs, memoryMb);
        verify(synthesis).validateAndNormalize(emptyItems);
        verify(writer).saveAll(evaluationId, emptyItems);
        verify(publisher).publish(evaluationId, submissionId, emptyItems, score, rubric, justification);
    }
}
