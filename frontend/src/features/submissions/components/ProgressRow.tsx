export default function ProgressRow({ label, value }: { label: string; value: number | undefined }) {
  const v = typeof value === "number" ? Math.max(0, Math.min(100, value)) : 0;
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs">
        <span className="font-medium">{label}</span>
        <span className="tabular-nums">{v}</span>
      </div>
      <div className="w-full h-2 rounded bg-gray-200 dark:bg-gray-700 overflow-hidden">
        <div className="h-2 rounded bg-gray-900 dark:bg-gray-100" style={{ width: `${v}%` }} role="progressbar" aria-valuenow={v} aria-valuemin={0} aria-valuemax={100}/>
      </div>
    </div>
  );
}
