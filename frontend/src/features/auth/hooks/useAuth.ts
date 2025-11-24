import { useEffect, useRef, useState } from "react";
import { AUTH_BYPASS } from "@/lib/config";
import { loginApi, registerApi, logoutApi } from "../services/authApi";
import { decodeJwt } from "@/lib/jwt";

export function useAuth() {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("tfb_token"));
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem("tfb_email"));
  const timerRef = useRef<number | null>(null);

  // Clears any scheduled auto-logout
  const clearLogoutTimer = () => {
    if (timerRef.current) {
      window.clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  };

  // Schedules auto-logout based on JWT 'exp'
  const scheduleAutoLogout = (tkn: string | null) => {
    clearLogoutTimer();
    const payload = decodeJwt(tkn);
    const exp = payload?.exp ? payload.exp * 1000 : null;
    if (!exp) return;
    const ms = exp - Date.now();
    if (ms <= 0) {
      // already expired → logout immediately
      logout();
      return;
    }
    timerRef.current = window.setTimeout(() => logout(), ms + 250);
  };

  useEffect(() => {
    scheduleAutoLogout(token);
    // synchronizes logout/login between tabs
    const onStorage = (e: StorageEvent) => {
      if (e.key === "tfb_token") {
        setToken(localStorage.getItem("tfb_token"));
        scheduleAutoLogout(localStorage.getItem("tfb_token"));
      }
      if (e.key === "tfb_email") setEmail(localStorage.getItem("tfb_email"));
    };
    window.addEventListener("storage", onStorage);
    return () => {
      window.removeEventListener("storage", onStorage);
      clearLogoutTimer();
    };
  }, []);

  const register = async (e: string, p: string) => {
    await registerApi(e, p);
    await login(e, p);
  };

  const login = async (e: string, p: string) => {
    if (AUTH_BYPASS) {
      const fake = `dev-${Math.random().toString(36).slice(2)}`;
      localStorage.setItem("tfb_token", fake);
      localStorage.setItem("tfb_email", e);
      setToken(fake); setEmail(e);
      scheduleAutoLogout(fake);
      return;
    }
    const data = await loginApi(e, p);
    localStorage.setItem("tfb_token", data.accessToken);
    localStorage.setItem("tfb_email", e);
    setToken(data.accessToken); setEmail(e);
    scheduleAutoLogout(data.accessToken);
  };

  const logout = async () => {
    try { await logoutApi(email ?? "", token ?? undefined); } catch {}
    localStorage.removeItem("tfb_token");
    localStorage.removeItem("tfb_email");
    clearLogoutTimer();
    setToken(null); setEmail(null);
  };

  return { token, email, register, login, logout, isAuth: !!token };
}