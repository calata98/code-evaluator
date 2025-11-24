
import { AuthorshipResult } from "../types";
import { getConfidenceToneClasses, getVerdictToneClasses } from "./AuthorshipToneUtils";

interface AuthorshipResultViewProps {
  result: AuthorshipResult;
  onClose: () => void;
}

export function AuthorshipResultView({ result, onClose }: AuthorshipResultViewProps) {
  return (
    <>
      <div className="space-y-3">
        <div className="text-sm flex items-center gap-2">
          <span className="font-semibold">Verdict:</span>
          <span
            className={
              "uppercase text-xs px-2 py-[3px] rounded border " +
              getVerdictToneClasses(result.verdict)
            }
          >
            {result.verdict.replace(/__/g, " ")}
          </span>
        </div>

        <div className="text-sm flex items-center gap-2">
          <span className="font-semibold">Confidence:</span>
          <span
            className={
              "tabular-nums font-semibold " +
              getConfidenceToneClasses(result.confidence)
            }
          >
            {(result.confidence * 100).toFixed(1)}%
          </span>
        </div>

        {result.justification && (
          <div className="text-sm">
            <div className="font-semibold mb-1">Justification:</div>
            <p className="leading-snug whitespace-pre-line">
              {result.justification}
            </p>
          </div>
        )}
      </div>

      <div className="mt-5 flex justify-end">
        <button
          onClick={onClose}
          className="text-sm px-4 py-2 rounded-md border border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800"
        >
          Cerrar
        </button>
      </div>
    </>
  );
}
