import { useEffect, useState } from "react";
import { API_BASE } from "@/lib/config";
import { AuthorshipResultView } from "./AuthorshipResultView";
import { AuthorshipTestForm } from "./AuthorshipTestForm";
import { AuthorshipTestModalProps } from "../types";

type AnswerMap = Record<string, number>; // questionId -> choiceIndex

// POST the answers to the backend, adapted to SubmitAnswersRequest
async function submitAuthorshipAnswers(
  token: string,
  submissionId: string,
  answers: AnswerMap
): Promise<void> {
  const res = await fetch(`${API_BASE}/authorship-tests/${submissionId}/answers`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`,
      "Content-Type": "application/json",
      "Accept": "application/json"
    },
    body: JSON.stringify({
      submissionId,
      answers
    })
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || "Error sending authorship test answers");
  }
}

export default function AuthorshipTestModal({
  token,
  mode,
  test,
  result,
  error,
  onClose,
  onSubmitted
}: AuthorshipTestModalProps) {
  const [selected, setSelected] = useState<AnswerMap>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Reset state when opening a new test / result
  useEffect(() => {
    if (!test && !result) {
      setSelected({});
      setSubmitError(null);
      setSubmitting(false);
      setSuccessMsg(null);
      return;
    }
    setSelected({});
    setSubmitError(null);
    setSubmitting(false);
    setSuccessMsg(null);
  }, [test?.id, result?.submissionId, mode]);

  // If there is nothing to show, do not render
  if (!test && !result) return null;

  const handleSelect = (questionId: string, idx: number) => {
    if (mode !== "test") return; // security: do not select in result mode

    setSelected(prev => ({
      ...prev,
      [questionId]: idx
    }));
    setSubmitError(null);
    setSuccessMsg(null);
  };

  const handleSubmit = async () => {
    if (mode !== "test" || !test) return;

    const answers: AnswerMap = {};
    for (const q of test.questions) {
      const idx = selected[q.id];
      if (typeof idx !== "number") {
        setSubmitError("You must select an option for all questions.");
        setSuccessMsg(null);
        return;
      }
      answers[q.id] = idx;
    }

    try {
      setSubmitting(true);
      setSubmitError(null);
      setSuccessMsg(null);

      await submitAuthorshipAnswers(token, test.submissionId, answers);

      setSuccessMsg("Answers sent successfully.");
      if (onSubmitted) {
        onSubmitted();
      }
    } catch (e: any) {
      setSubmitError(e.message || "Error sending answers.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    onClose();
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4"
      onClick={handleClose}
    >
      <div
        className="bg-white dark:bg-gray-900 rounded-2xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-5"
        onClick={e => e.stopPropagation()}
      >

        {mode === "result" && result && (
          <AuthorshipResultView result={result} onClose={handleClose} />
        )}

        {mode === "test" && test && (
          <AuthorshipTestForm
            test={test}
            selected={selected}
            submitting={submitting}
            submitError={submitError}
            successMsg={successMsg}
            onSelect={handleSelect}
            onSubmit={handleSubmit}
            onCancel={handleClose}
          />
        )}
      </div>
    </div>
  );
}
