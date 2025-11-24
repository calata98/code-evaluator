package com.calata.evaluator.kafkaconfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaTopicsProperties {

    private String submissions = "submissions-topic";
    private String submissionStatus = "submission-status-topic";
    private String executionRequests = "execution-requests-topic";
    private String executionResults = "execution-results-topic";
    private String evaluationCreated = "evaluation-created-topic";
    private String aiFeedbackRequested = "ai-feedback-requested-topic";
    private String aiFeedbackCreated = "ai-feedback-created-topic";
    private String similarityComputed = "similarity-computed-topic";
    private String authorshipAnswersProvided = "authorship-answers-provided-topic";
    private String authorshipTestCreated = "authorship-test-created-topic";
    private String authorshipEvaluationComputed = "authorship-evaluation-computed-topic";
    private String stepFailed = "step-failed-topic";
}
