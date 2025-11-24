import { useEffect, useMemo, useRef, useState } from "react";
import { fetchMySubmissions } from "../services/submissionsApi";
import { getSeverityClass } from "@/lib/severity";
import ProgressRow from "../components/ProgressRow";
import { API_BASE } from "@/lib/config";
import { sseClient, SseEvent } from "@/features/auth/hooks/sseClient";
import AuthorshipTestModal from "../components/AuthorshipTestModal";
import Toast from "@/components/Toast";
import { RUBRIC_ORDER, SubmissionDetailView } from "@/types/domain";
import { getScoreToneClass } from "../components/AuthorshipToneUtils";

const TERMINAL_SUBMISSION = new Set(["COMPLETED"]);

async function fetchAuthorshipTestBySubmission(
  token: string,
  submissionId: string
): Promise<AuthorshipTest | null> {
  const res = await fetch(`${API_BASE}/authorship-tests/${submissionId}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: "application/json",
    },
  });

  if (res.status === 404) return null;
  if (!res.ok) {
    throw new Error("Error loading authorship test");
  }

  return res.json();
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

async function fetchAuthorshipResultBySubmission(
  token: string,
  submissionId: string
): Promise<AuthorshipResult | null> {
  const res = await fetch(
    `${API_BASE}/authorship-evaluations/${submissionId}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "application/json",
      },
    }
  );

  if (res.status === 404 || res.status === 204) {
    return null;
  }

  if (!res.ok) {
    throw new Error("Error loading authorship evaluation");
  }

  const text = await res.text();
  if (!text) {
    return null;
  }

  return JSON.parse(text) as AuthorshipResult;
}

interface AuthorshipQuestion {
  id: string;
  prompt: string;
  choices: string[];
}

interface AuthorshipTest {
  id: string;
  submissionId: string;
  language: string;
  createdAt: string;
  expiresAt: string;
  questions: AuthorshipQuestion[];
}

export default function MySubmissionsPage({ token }: { token: string }) {
  const [items, setItems] = useState<SubmissionDetailView[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expanded, setExpanded] = useState<Record<string, boolean>>({});
  const intervalRef = useRef<number | null>(null);
  const [authTest, setAuthTest] = useState<AuthorshipTest | null>(null);
  const [authResult, setAuthResult] = useState<AuthorshipResult | null>(null);
  const [authTestError, setAuthTestError] = useState<string | null>(null);
  const [authMode, setAuthMode] = useState<"test" | "result">("test");

  const [toastId, setToastId] = useState<string | null>(null);
  const [showToast, setShowToast] = useState(false);

  const [authResultsBySubmission, setAuthResultsBySubmission] =
    useState<Record<string, AuthorshipResult | null>>({});

  const openAuthorshipForSubmission = async (submissionId: string) => {
    setAuthTest(null);
    setAuthResult(null);
    setAuthTestError(null);

    try {
      const result = await fetchAuthorshipResultBySubmission(
        token,
        submissionId
      );

      if (result) {
        setAuthMode("result");
        setAuthResult(result);
        setAuthResultsBySubmission((prev) => ({
          ...prev,
          [submissionId]: result,
        }));
        return;
      }

      const test = await fetchAuthorshipTestBySubmission(token, submissionId);

      if (test) {
        setAuthMode("test");
        setAuthTest(test);
        return;
      }

      setAuthTestError("No authorship test associated with this submission.");
    } catch (e: any) {
      console.error(e);
      setAuthTestError(e.message || "Error loading authorship data");
    }
  };

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      setItems(await fetchMySubmissions(token));
    } catch (e: any) {
      setError(e.message || "Error loading submissions");
    } finally {
      setLoading(false);
    }
  };

  const shouldPoll = useMemo(() => {
    if (!items || items.length === 0) return false;
    const anyNonTerminal = items.some(
      ({ submission }) =>
        !TERMINAL_SUBMISSION.has((submission.status || "").toUpperCase())
    );
    const anyPendingEval = items.some(({ evaluations }) =>
      evaluations.length === 0 ||
      evaluations.some((ev) => typeof ev.score !== "number")
    );
    return anyNonTerminal || anyPendingEval;
  }, [items]);

  useEffect(() => {
    fetchData();
  }, [token]);

  useEffect(() => {
    if (intervalRef.current) {
      window.clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    if (shouldPoll) {
      intervalRef.current = window.setInterval(() => {
        fetchData();
      }, 5000);
    }
    return () => {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    };
  }, [shouldPoll]);

  useEffect(() => {
    const onVis = () => {
      if (document.visibilityState === "visible") fetchData();
    };
    document.addEventListener("visibilitychange", onVis);
    window.addEventListener("focus", onVis);
    return () => {
      document.removeEventListener("visibilitychange", onVis);
      window.removeEventListener("focus", onVis);
    };
  }, []);

  useEffect(() => {
    if (!token) return;

    const off = sseClient.on(({ type, data }: SseEvent) => {
      if (type === "authorship-test-created") {
        console.info("Authorship test ready:", data);
        setToastId(data.submissionId);
        setShowToast(true);
        setTimeout(() => setShowToast(false), 10000);
        fetchData();
      } else if (type === "authorship-evaluation-completed") {
        console.info("Authorship evaluation completed:", data);
        fetchData();
      } else {
        console.debug("Evento SSE desconocido:", type, data);
      }
    });

    sseClient.connect(`${API_BASE}/sse`, token);

    return () => {
      off();
      sseClient.disconnect();
    };
  }, [token]);


  useEffect(() => {
    if (!items) return;

    items.forEach(({ submission: s }) => {
      if (!s.hasAuthorshipTest) return;

      const existing = authResultsBySubmission[s.id];
      if (existing) return;

      fetchAuthorshipResultBySubmission(token, s.id)
        .then((res) => {
          if (res) {
            setAuthResultsBySubmission((prev) => ({
              ...prev,
              [s.id]: res,
            }));
          }
        })
        .catch((err) => {
          console.error("Error loading authorship evaluation for", s.id, err);
        });
    });
}, [items, token]);

  const toggle = (id: string) =>
    setExpanded((prev) => ({ ...prev, [id]: !prev[id] }));

  const closeAuthTestModal = () => {
    setAuthTest(null);
    setAuthResult(null);
    setAuthTestError(null);
  };

  if (loading && !items)
    return <p className="text-sm text-gray-600">Loading...</p>;
  if (error) return <p className="text-sm text-red-600">{error}</p>;
  if (!items || items.length === 0)
    return <p className="text-sm text-gray-600">No submissions yet.</p>;

  return (
    <>
      <ul className="space-y-3">
        {items.map(({ submission: s, evaluations }) => {
          const latestEval =
            evaluations && evaluations.length > 0 ? evaluations[0] : null;
          const authRes = authResultsBySubmission[s.id];
          const hasAuthEval = s.hasAuthorshipEvaluation || !!authRes;
          const isLikelyNotAuthor = authRes?.verdict === "LIKELY_NOT_AUTHOR";

          return (
            <li
              key={s.id}
              className="border rounded-xl bg-white dark:bg-gray-800 dark:border-gray-700 shadow-sm"
            >
              <button
                onClick={() => toggle(s.id)}
                className="w-full text-left px-4 py-3 flex items-center justify-between"
              >
                <div>
                  <div className="font-medium">{s.title}</div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    {s.language} • {s.status} •{" "}
                    {new Date(s.createdAt).toLocaleString()}
                  </div>

                  {s.status === "COMPLETED" &&
                    typeof latestEval?.score === "number" && (
                      <div className="mt-1 text-xs text-gray-700 dark:text-gray-200">
                        Score:{" "}
                        <span
                          className={`font-semibold tabular-nums ${getScoreToneClass(
                            latestEval.score
                          )}`}
                        >
                          {latestEval.score}
                        </span>
                        /100
                      </div>
                    )}
                </div>
                <span className="text-sm text-gray-500">
                  {expanded[s.id] ? "▲" : "▼"}
                </span>
              </button>

              {expanded[s.id] && (
                <div className="px-4 pb-4 grid gap-4">
                  <div className="border rounded-lg p-3 dark:border-gray-700">
                    <div className="font-medium mb-2 flex items-center gap-2 justify-between">
                      <span>Evaluations</span>

                      <div className="ml-auto flex items-center gap-2">
                        {authRes && (
                          <span
                            className={`text-[11px] font-semibold ${
                              authRes.verdict === "LIKELY_NOT_AUTHOR"
                                ? "text-red-600 dark:text-red-400"
                                : authRes.verdict === "LIKELY_AUTHOR"
                                ? "text-emerald-600 dark:text-emerald-400"
                                : "text-amber-600 dark:text-amber-400"
                            }`}
                          >
                            {authRes.verdict.replace(/_/g, " ")}
                          </span>
                        )}

                        {s.hasAuthorshipTest && (
                          <button
                            type="button"
                            onClick={() => openAuthorshipForSubmission(s.id)}
                            className={
                              hasAuthEval
                                ? "text-xs px-2 py-1 rounded border border-amber-500 text-amber-600 hover:bg-amber-50 dark:border-amber-400 dark:text-amber-300 dark:hover:bg-amber-900/40"
                                : "text-xs px-2 py-1 rounded bg-amber-500 text-white hover:bg-amber-600 dark:bg-amber-400 dark:text-gray-900 dark:hover:bg-amber-300"
                            }
                          >
                            {hasAuthEval ? "Authorship Test Evaluation" : "Authorship Test"}
                          </button>
                        )}
                      </div>
                    </div>

                    {evaluations.length === 0 ? (
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        No evaluations yet.
                      </p>
                    ) : (
                      <ul className="space-y-3">
                        {evaluations.map((ev) => (
                          <li
                            key={ev.id}
                            className="border rounded p-3 dark:border-gray-700"
                          >
                            <div className="flex flex-wrap items-center gap-2 text-sm">
                              <span className="text-[11px] text-gray-500">
                                {new Date(
                                  ev.createdAt
                                ).toLocaleString()}
                              </span>
                              {typeof ev.score === "number" && (
                                <span className="ml-auto text-sm">
                                  Score:{" "}
                                  <b className="tabular-nums">
                                    {ev.score}
                                  </b>
                                  /100
                                </span>
                              )}
                            </div>

                            {ev.rubric && (
                              <div className="mt-3 grid gap-2">
                                <div className="text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400">
                                  Rubric
                                </div>
                                <div className="grid sm:grid-cols-2 gap-3">
                                  {RUBRIC_ORDER.map((k) => (
                                    <ProgressRow
                                      key={k}
                                      label={k.replace("_", " ")}
                                      value={ev.rubric?.[k]}
                                    />
                                  ))}
                                </div>
                              </div>
                            )}

                            {ev.justification?.trim() && (
                              <div className="mt-3">
                                <div className="text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400 mb-1">
                                  Justification
                                </div>
                                <p className="text-sm leading-snug">
                                  {ev.justification}
                                </p>
                              </div>
                            )}

                            {ev.feedbacks &&
                              ev.feedbacks.length > 0 && (
                                <div className="mt-4 overflow-x-auto">
                                  <table className="min-w-full text-sm border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
                                    <thead className="bg-gray-50 dark:bg-gray-800/60">
                                      <tr>
                                        <th className="px-3 py-2 font-semibold border-b dark:border-gray-700 text-center align-middle">
                                          Type
                                        </th>
                                        <th className="px-3 py-2 font-semibold border-b dark:border-gray-700 text-center align-middle">
                                          Severity
                                        </th>
                                        <th className="px-3 py-2 text-left font-semibold border-b dark:border-gray-700">
                                          Title
                                        </th>
                                        <th className="px-3 py-2 text-left font-semibold border-b dark:border-gray-700">
                                          Message
                                        </th>
                                        <th className="px-3 py-2 text-left font-semibold border-b dark:border-gray-700">
                                          Suggestion
                                        </th>
                                      </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                                      {ev.feedbacks.map((f) => (
                                        <tr key={f.id}>
                                          <td className="px-3 py-2 text-center align-middle">
                                            <span className="text-[11px] px-2 py-[3px] border rounded capitalize dark:border-gray-600 inline-block">
                                              {f.type || "general"}
                                            </span>
                                          </td>
                                          <td className="px-3 py-2 text-center align-middle">
                                            <span
                                              className={`text-[11px] px-2 py-[3px] rounded ${getSeverityClass(
                                                f.severity || "INFO"
                                              )} inline-block`}
                                            >
                                              {f.severity || "INFO"}
                                            </span>
                                          </td>
                                          <td className="px-3 py-2 align-top font-medium">
                                            {f.title ?? "—"}
                                          </td>
                                          <td className="px-3 py-2 align-top">
                                            {f.message}
                                          </td>
                                          <td className="px-3 py-2 align-top">
                                            {f.suggestion ?? "—"}
                                          </td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                              )}
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                </div>
              )}
            </li>
          );
        })}
      </ul>

      <AuthorshipTestModal
        token={token}
        mode={authMode}
        test={authTest}
        result={authResult}
        error={authTestError}
        onClose={closeAuthTestModal}
        onSubmitted={() => {
          closeAuthTestModal();
          fetchData();
        }}
      />
      {showToast && (
        <Toast
          title="High similarity detected"
          variant="warning"
          onClose={() => setShowToast(false)}
        >
          An authorship test has been created for submission{" "}
          <span className="font-mono">{toastId}</span>.
        </Toast>
      )}
    </>
  );
}
