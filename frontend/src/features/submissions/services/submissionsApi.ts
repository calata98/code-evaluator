import { api } from "@/lib/api";
import { API_BASE } from "@/lib/config";
import type { SubmissionDetailView } from "@/types/domain";

export interface CreateSubmissionResponse { submissionId: string }

export async function createSubmission(
  token: string, payload: { title: string; language: string; code: string }
) {
  return api<CreateSubmissionResponse>(API_BASE, "/submissions", "POST", payload, token);
}

export async function fetchMySubmissions(token: string) {
  return api<SubmissionDetailView[]>(API_BASE, "/me/submissions", "GET", undefined, token);
}