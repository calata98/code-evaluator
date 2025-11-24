import { AuthorshipTest } from "../types";

type AnswerMap = Record<string, number>;

interface AuthorshipTestFormProps {
  test: AuthorshipTest;
  selected: AnswerMap;
  submitting: boolean;
  submitError: string | null;
  successMsg: string | null;
  onSelect: (questionId: string, idx: number) => void;
  onSubmit: () => void;
  onCancel: () => void;
}

export function AuthorshipTestForm({
  test,
  selected,
  submitting,
  submitError,
  successMsg,
  onSelect,
  onSubmit,
  onCancel
}: AuthorshipTestFormProps) {
  const allAnswered =
    test.questions.every(q => selected[q.id] !== undefined);

  return (
    <>
      {submitError && (
        <p className="mb-2 text-sm text-red-600">
          {submitError}
        </p>
      )}

      {successMsg && (
        <p className="mb-2 text-sm text-green-600">
          {successMsg}
        </p>
      )}

      <div className="space-y-4">
        {test.questions.map((q, index) => (
          <div
            key={q.id}
            className="border border-gray-200 dark:border-gray-700 rounded-lg p-3"
          >
            <div className="flex items-start gap-2 mb-2">
              <span className="mt-[2px] text-xs font-semibold text-gray-500 dark:text-gray-400">
                Q{index + 1}.
              </span>
              <p className="text-sm font-medium">{q.prompt}</p>
            </div>

            <ul className="space-y-1 text-sm">
              {q.choices.map((choice, i) => {
                const isSelected = selected[q.id] === i;
                return (
                  <li key={i}>
                    <button
                      type="button"
                      onClick={() => onSelect(q.id, i)}
                      className={`w-full flex items-start gap-2 rounded-md px-2 py-1 text-left border
                        ${
                          isSelected
                            ? "border-blue-500 bg-blue-50 dark:bg-blue-900/40"
                            : "border-transparent hover:bg-gray-50 dark:hover:bg-gray-800"
                        }`}
                    >
                      <span className="mt-[2px] text-xs font-semibold">
                        {String.fromCharCode(65 + i)}.
                      </span>
                      <span>{choice}</span>
                    </button>
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </div>

      <div className="mt-5 flex justify-end gap-2">
        <button
          onClick={onCancel}
          className="text-sm px-4 py-2 rounded-md border border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
          disabled={submitting}
        >
          Cancelar
        </button>
        <button
          onClick={onSubmit}
          className="text-sm px-4 py-2 rounded-md bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed"
          disabled={submitting || !allAnswered}
        >
          {submitting ? "Sending..." : "Send Answers"}
        </button>
      </div>
    </>
  );
}
