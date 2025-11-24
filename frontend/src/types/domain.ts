export type FeedbackType =
  | "READABILITY" | "CORRECTNESS" | "PERFORMANCE"
  | "SECURITY" | "BEST_PRACTICE" | "COMPLEXITY";

export interface FeedbackItem {
  id: string; title: string; message: string;
  type: "STYLE"|"PERFORMANCE"|"CORRECTNESS"|"SECURITY"|"READABILITY"|"BEST_PRACTICE"|"COMPLEXITY" | string;
  severity: "INFO"|"MINOR"|"MAJOR"|"CRITICAL"|"BLOCKER" | string; suggestion?: string;
}

export interface EvaluationView {
  id: string; status: string; score?: number; createdAt: string;
  feedbacks?: FeedbackItem[];
  rubric?: Record<FeedbackType, number>;
  justification?: string;
}

export interface SubmissionSummary {
  id: string; userId: string; title: string; language: string; status: string; createdAt: string, 
  hasAuthorshipTest: boolean, hasAuthorshipEvaluation: boolean;
}

export interface SubmissionDetailView {
  submission: SubmissionSummary;
  evaluations: EvaluationView[];
  lastUpdated: string;
}

export const RUBRIC_ORDER: FeedbackType[] = [
  "READABILITY","CORRECTNESS","PERFORMANCE","SECURITY","BEST_PRACTICE","COMPLEXITY",
];
