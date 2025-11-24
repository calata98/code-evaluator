import { useEffect, useState } from "react";
import { useAuth } from "@/features/auth/hooks/useAuth";
import TopBar from "./layout/TopBar";
import Tabs from "./layout/Tabs";
import LoginForm from "@/features/auth/components/LoginForm";
import SubmitCodePage from "@/features/submissions/pages/SubmitCodePage";
import MySubmissionsPage from "@/features/submissions/pages/MySubmissionsPage";
import { onUnauthorized } from "@/lib/api";

export default function App() {
  const auth = useAuth();
  const [tab, setTab] = useState("mine");
  const [loggingIn, setLoggingIn] = useState(false);

  useEffect(() => {
    onUnauthorized(() => auth.logout());
  }, [auth]);

  const onLogin = async (email: string, password: string) => {
    setLoggingIn(true);
    try { await auth.login(email, password); setTab("submit"); }
    finally { setLoggingIn(false); }
  };

  return (
    <div className={["min-h-screen", "bg-gray-100 dark:bg-gray-900 dark:text-gray-100"].join(" ")}>
      {auth.isAuth ? (
        <>
          <TopBar email={auth.email} onLogout={auth.logout} />
          <Tabs tab={tab} setTab={setTab} />
          <main className="max-w-5xl mx-auto px-4 py-6">
            {tab === "submit" && auth.token && <SubmitCodePage token={auth.token} />}
            {tab === "mine"   && auth.token && <MySubmissionsPage token={auth.token} />}
          </main>
        </>
      ) : (
        <>
          <div className="w-full border-b bg-white dark:bg-gray-800 dark:border-gray-700">
            <div className="block mx-auto px-4 py-3"><div className="font-semibold">TFB Code Evaluator</div></div>
          </div>
          <LoginForm onLogin={onLogin} onRegister={auth.register} loading={loggingIn} />
        </>
      )}
      <footer className="text-center text-[11px] text-gray-500 py-6">TFB • Code Evaluator</footer>
    </div>
  );
}
