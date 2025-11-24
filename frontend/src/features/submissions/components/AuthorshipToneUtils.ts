export function getConfidenceToneClasses(confidence: number): string {
  if (confidence < 0.4) {
    return "text-red-600 dark:text-red-400";
  }
  if (confidence < 0.7) {
    return "text-amber-600 dark:text-amber-400";
  }
  return "text-green-600 dark:text-green-400";
}

export function getVerdictToneClasses(verdict: string): string {
  switch (verdict) {
    case "LIKELY_AUTHOR":
      return "bg-green-100 text-green-800 border-green-300 dark:bg-green-900/40 dark:text-green-300 dark:border-green-700";
    case "UNCERTAIN":
      return "bg-amber-100 text-amber-800 border-amber-300 dark:bg-amber-900/40 dark:text-amber-300 dark:border-amber-700";
    case "LIKELY_NOT_AUTHOR":
      return "bg-red-100 text-red-800 border-red-300 dark:bg-red-900/40 dark:text-red-300 dark:border-red-700";
    default:
      return "bg-gray-100 text-gray-800 border-gray-300 dark:bg-gray-800 dark:text-gray-200 dark:border-gray-600";
  }
}

export function getScoreToneClass(score: number): string {
  if (score >= 80) {
    return "text-emerald-600 dark:text-emerald-400";
  }
  if (score >= 50) {
    return "text-amber-600 dark:text-amber-400";
  }
  return "text-red-600 dark:text-red-400";
}
