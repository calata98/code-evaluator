import { classNames } from "@/lib/classNames";

export default function Tabs({ tab, setTab }: { tab: string; setTab: (t: string) => void }) {
  const items = [
    { id: "mine", label: "My Submissions" },
    { id: "submit", label: "New Submission" },
  ];
  return (
    <div className="border-b bg-gray-50 dark:bg-gray-900 dark:border-gray-700">
      <div className="max-w-5xl mx-auto px-4">
        <nav className="flex gap-2">
          {items.map(it => (
            <button key={it.id} onClick={() => setTab(it.id)}
              className={classNames("px-4 py-2 -mb-px border-b-2",
                tab === it.id ? "border-black font-medium"
                               : "border-transparent text-gray-500 hover:text-gray-800 dark:text-gray-300 dark:hover:text-gray-100")}>
              {it.label}
            </button>
          ))}
        </nav>
      </div>
    </div>
  );
}
