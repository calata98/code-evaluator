package com.calata.evaluator.contracts.events;

public enum StepNames {
    // Submission Steps
    SUBMISSION_CREATED("Submission created"),
    SUBMISSION_STATUS_UPDATE("Submission status update"),

    // Evaluation Steps
    EVALUATION_CREATED("Evaluation created"),
    EVALUATION_COMPLETED("Evaluation Completed"),
    EVALUATION_REQUEST("Execution Request"),

    // Execution Steps
    EXECUTION_REQUEST("Execution Request"),
    EXECUTION_RESULT("Execution Result"),

    // AIFeedback Steps
    AI_FEEDBACK_REQUEST("AI Feedback Request"),
    AI_FEEDBACK_CREATED("AI Feedback Created"),

    // Similarity Steps
    SIMILARITY_COMPUTED("Similarity Computed"),

    // Authorship Steps
    AUTHORSHIP_TEST_CREATED("Authorship Test Created"),
    AUTHORSHIP_ANSWERS_PROVIDED("Authorship Answers Provided");

    StepNames(String name) {
    }

    public static String getErrorCode(StepNames step) {
        return step.name() + "_ERROR";
    }
}
