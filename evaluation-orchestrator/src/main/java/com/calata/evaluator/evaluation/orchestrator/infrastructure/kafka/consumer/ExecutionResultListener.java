package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.ExecutionResult;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessExecutionResultCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleExecutionResultUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ExecutionResultListener {

    private final HandleExecutionResultUseCase useCase;

    public ExecutionResultListener(HandleExecutionResultUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.executionResults}", groupId = "evaluation-orchestrator")
    public void onMessage(ExecutionResult msg){
        useCase.handle(new ProcessExecutionResultCommand(
                msg.submissionId(),
                msg.stdout(), msg.stderr(),
                msg.timeMs(), msg.memoryMb()
        ));
    }
}
