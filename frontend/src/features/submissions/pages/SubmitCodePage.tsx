import { useRef, useState } from "react";
import Toast from "@/components/Toast";
import { createSubmission } from "../services/submissionsApi";

export default function SubmitCodePage({ token }: { token: string }) {
  const formRef = useRef<HTMLFormElement>(null);
  const [title, setTitle] = useState("");
  const [language, setLanguage] = useState("JAVA");
  const [code, setCode] = useState(
    "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello world!\");\n  }\n}"
  );
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [toastId, setToastId] = useState<string | null>(null);
  const [showToast, setShowToast] = useState(false);

  function openToast() { setShowToast(true); setTimeout(() => setShowToast(false), 5000); }

  const doSubmit = async () => {
    setSaving(true); setErr(null);
    try {
      const res = await createSubmission(token, { title, language, code });
      setToastId(res.submissionId);
      openToast();
      setTitle(""); setLanguage("JAVA"); setCode("");
    } catch (e: any) {
      setErr(e.message || "Error submitting code");
    } finally { setSaving(false); }
  };

  const onSubmit = (ev: React.FormEvent<HTMLFormElement>) => { ev.preventDefault(); setShowConfirm(true); };

  const handleAskConfirm = () => {
    const form = formRef.current;
    if (form && !form.checkValidity()) { form.reportValidity(); return; }
    setShowConfirm(true);
  };

  return (
    <form ref={formRef} onSubmit={onSubmit} className="space-y-4">
      {/* Title */}
      <div>
        <label className="block text-sm mb-1">Title</label>
        <input className="w-full border rounded-md px-3 py-2 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-100"
               value={title} onChange={e=>setTitle(e.target.value)} required/>
      </div>
      {/* Language */}
      <div>
        <label className="block text-sm mb-1">Language</label>
        <select className="w-full border rounded-md px-3 py-2 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-100"
                value={language} onChange={e=>setLanguage(e.target.value)}>
          <option value="JAVA">Java</option>
          <option value="PYTHON">Python</option>
        </select>
      </div>
      {/* Code */}
      <div>
        <label className="block text-sm mb-1">Code</label>
        <textarea className="w-full border rounded-md px-3 py-2 font-mono text-sm min-h-[420px] dark:bg-gray-900 dark:border-gray-700 dark:text-gray-100"
                  value={code} onChange={e=>setCode(e.target.value)} required/>
      </div>

      {err && <div className="text-red-700 text-sm">{err}</div>}

      <button type="button" onClick={handleAskConfirm} disabled={saving}
        className="block mx-auto rounded-md px-4 py-2 bg-black text-white hover:bg-gray-900 active:scale-95 active:opacity-90
                   focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-black focus-visible:ring-offset-2
                   dark:bg-white dark:text-black dark:hover:bg-gray-100 dark:focus-visible:ring-white transition transform duration-150 disabled:opacity-60">
        {saving ? "Submitting…" : "Submit Code"}
      </button>

      {/* Confirm modal */}
      {showConfirm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-xl max-w-sm w-full">
            <h2 className="text-lg font-semibold mb-4 text-center">¿Confirm submission?</h2>
            <p className="text-sm text-gray-600 dark:text-gray-300 text-center mb-4">Your code will be sent for automatic evaluation.</p>
            <div className="flex justify-center gap-4">
              <button onClick={() => setShowConfirm(false)} className="px-4 py-2 border rounded-md dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700">Cancel</button>
              <button onClick={() => { setShowConfirm(false); doSubmit(); }} className="px-4 py-2 bg-black text-white rounded-md dark:bg-white dark:text-black">Confirm</button>
            </div>
          </div>
        </div>
      )}

      {showToast && (
        <Toast title="Submission sent" onClose={() => setShowToast(false)}>
          {toastId && <>ID: <span className="font-mono">{toastId}</span></>}
        </Toast>
      )}
    </form>
  );
}
