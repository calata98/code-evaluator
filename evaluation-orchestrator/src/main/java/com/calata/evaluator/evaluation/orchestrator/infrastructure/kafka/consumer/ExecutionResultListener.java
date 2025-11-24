package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.ExecutionResult;
import com.calata.evaluator.contracts.events.StepFailedEvent;
import com.calata.evaluator.contracts.events.StepNames;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessExecutionResultCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleExecutionResultUseCase;
import com.calata.evaluator.kafkaconfig.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExecutionResultListener {

    private final HandleExecutionResultUseCase useCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    private static final Logger logger = LoggerFactory.getLogger(ExecutionResultListener.class);

    public ExecutionResultListener(HandleExecutionResultUseCase useCase, KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties kafkaTopicsProperties) {
        this.useCase = useCase;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.executionResults}", groupId = "${app.kafka.group}")
    public void onMessage(ExecutionResult msg){
        try {
            useCase.handle(new ProcessExecutionResultCommand(
                    msg.submissionId(),
                    msg.stdout(), msg.stderr(),
                    msg.timeMs(), msg.memoryMb()
            ));
        } catch (Exception e) {
            logger.error("Error processing ExecutionResult message", e);
            publishStepFailed(msg.submissionId(), e.getMessage());
        }
    }

    private void publishStepFailed(String submissionId, String errorMessage) {
        StepNames stepName = StepNames.EXECUTION_RESULT;
        StepFailedEvent event = new StepFailedEvent(
                submissionId,
                stepName.name(),
                StepNames.getErrorCode(stepName),
                errorMessage
        );
        kafkaTemplate.send(kafkaTopicsProperties.getStepFailed(), event);
    }
}
