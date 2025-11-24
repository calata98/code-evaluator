export const severityClasses: Record<string, string> = {
  INFO:     "bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200 border border-blue-300/60 dark:border-blue-700/60",
  MINOR:    "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-200 border border-emerald-300/60 dark:border-emerald-700/60",
  MAJOR:    "bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-200 border border-amber-300/60 dark:border-amber-700/60",
  CRITICAL: "bg-orange-100 text-orange-900 dark:bg-orange-900/40 dark:text-orange-200 border border-orange-300/60 dark:border-orange-700/60",
  BLOCKER:  "bg-red-100 text-red-900 dark:bg-red-900/40 dark:text-red-200 border border-red-300/60 dark:border-red-700/60",
};

export function getSeverityClass(sev?: string) {
  if (!sev) return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200 border border-gray-300/60 dark:border-gray-700/60";
  return severityClasses[sev.toUpperCase()] ?? "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200 border border-gray-300/60 dark:border-gray-700/60";
}