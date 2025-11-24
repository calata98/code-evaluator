export interface AuthorshipQuestion {
  id: string;
  prompt: string;
  choices: string[];
}

export interface AuthorshipTest {
  submissionId: string;
  language: string;
  createdAt: string;
  expiresAt: string;
  questions: AuthorshipQuestion[];
}

export interface AuthorshipResult {
  submissionId: string;
  userId: string;
  language: string;
  confidence: number;
  verdict: string;
  justification: string;
  createdAt: string;
}

export interface AuthorshipQuestion {
  id: string;
  prompt: string;
  choices: string[];
}

export interface AuthorshipTest {
  id: string;
  submissionId: string;
  language: string;
  createdAt: string;
  expiresAt: string;
  questions: AuthorshipQuestion[];
}

export interface AuthorshipResult {
  submissionId: string;
  userId: string;
  language: string;
  confidence: number;
  verdict: string;
  justification: string;
  createdAt: string;
}

export interface AuthorshipTestModalProps {
  token: string;
  mode: "test" | "result";
  test: AuthorshipTest | null;
  result: AuthorshipResult | null;
  error?: string | null;
  onClose: () => void;
  onSubmitted?: () => void;
}