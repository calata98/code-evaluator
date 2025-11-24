import { api } from "@/lib/api";
import { AUTH_API } from "@/lib/config";

export interface LoginResponse { accessToken: string }

export async function loginApi(email: string, password: string) {
  return api<LoginResponse>(AUTH_API, "/login", "POST", { email, password });
}

export async function registerApi(email: string, password: string) {
  await api<void>(AUTH_API, "/register", "POST", { email, password });
}

export async function logoutApi(email: string, token?: string | null) {
  try { await api<void>(AUTH_API, "/logout", "POST", { email }, token ?? undefined); }
  catch {}
}
