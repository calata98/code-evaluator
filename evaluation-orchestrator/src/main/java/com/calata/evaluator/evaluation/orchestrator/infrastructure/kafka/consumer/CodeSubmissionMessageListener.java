package com.calata.evaluator.evaluation.orchestrator.infrastructure.kafka.consumer;

import com.calata.evaluator.contracts.events.SubmissionCreated;
import com.calata.evaluator.evaluation.orchestrator.application.command.ProcessCodeSubmissionCommand;
import com.calata.evaluator.evaluation.orchestrator.application.port.in.HandleCodeSubmissionUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CodeSubmissionMessageListener {

    private final HandleCodeSubmissionUseCase useCase;

    public CodeSubmissionMessageListener(HandleCodeSubmissionUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.submissions}", groupId = "submission-group")
    public void onMessage(SubmissionCreated msg){
        useCase.handle(new ProcessCodeSubmissionCommand(
                msg.id(), msg.code(), msg.language(), msg.userId()
        ));
    }
}
