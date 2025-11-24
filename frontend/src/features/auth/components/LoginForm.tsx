import { useState } from "react";

export default function LoginForm({
  onRegister, onLogin, loading,
}: { onRegister: (e: string, p: string) => void; onLogin: (e: string, p: string) => void; loading: boolean }) {
  const [email, setEmail] = useState(""); const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [mode, setMode] = useState<"login"|"register">("login");

  async function submit(ev: React.FormEvent) {
    ev.preventDefault(); setError(null);
    try { mode === "login" ? await onLogin(email, password) : await onRegister(email, password); }
    catch (e: any) { setError(e?.message || (mode === "login" ? "Login error" : "Register error")); }
  }

  return (
    <div className="min-h-[70vh] grid place-items-center">
      <form onSubmit={submit} className="w-full max-w-md border p-6 rounded-xl bg-white dark:bg-gray-800 dark:border-gray-700 space-y-4 shadow-sm">
        <h1 className="text-xl font-semibold">{mode === "login" ? "Login" : "Register"}</h1>
        <div>
          <label className="block text-sm mb-1">Email</label>
          <input className="w-full border rounded-md px-3 py-2 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-100" type="email" value={email} onChange={e=>setEmail(e.target.value)} required />
        </div>
        <div>
          <label className="block text-sm mb-1">Password</label>
          <input className="w-full border rounded-md px-3 py-2 dark:bg-gray-900 dark:border-gray-700 dark:text-gray-100" type="password" value={password} onChange={e=>setPassword(e.target.value)} required />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button disabled={loading} className="w-full rounded-md px-3 py-2 bg-black text-white dark:bg-white dark:text-black disabled:opacity-60">
          {loading ? (mode === "login" ? "Logging in…" : "Creating…") : (mode === "login" ? "Login" : "Create account")}
        </button>
        <div className="text-xs text-center text-gray-600 dark:text-gray-300">
          {mode === "login" ? (
            <>No account? <button type="button" className="underline" onClick={() => setMode("register")}>Create one</button></>
          ) : (
            <>Already have an account? <button type="button" className="underline" onClick={() => setMode("login")}>Login</button></>
          )}
        </div>
      </form>
    </div>
  );
}
