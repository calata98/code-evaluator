package com.calata.evaluator.authorship.infrastructure.kafka.consumer;

import com.calata.evaluator.authorship.application.command.ProcessAuthorshipAnswersCommand;
import com.calata.evaluator.authorship.application.port.in.HandleAuthorshipAnswersUseCase;
import com.calata.evaluator.contracts.events.AuthorshipAnswersProvided;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuthorshipAnswersListener {

    private final HandleAuthorshipAnswersUseCase useCase;

    public AuthorshipAnswersListener(HandleAuthorshipAnswersUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.authorshipAnswersProvided}", groupId = "authorship-group")
    public void onMessage(AuthorshipAnswersProvided evt) {
        var cmd = new ProcessAuthorshipAnswersCommand(evt.submissionId(), evt.userId(), evt.answers());
        useCase.handle(cmd);
    }
}
