export default function TopBar({ email, onLogout }: { email: string | null; onLogout: () => void }) {
  return (
    <div className="w-full border-b bg-white dark:bg-gray-800 dark:border-gray-700">
      <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
        <div className="font-semibold">TFB Code Evaluator</div>
        <div className="flex items-center gap-3">
          <span className="text-sm text-gray-600 dark:text-gray-300">{email}</span>
          <button className="px-3 py-1 rounded-md border hover:bg-gray-50 dark:hover:bg-gray-700 dark:border-gray-600" onClick={onLogout}>Exit</button>
        </div>
      </div>
    </div>
  );
}
