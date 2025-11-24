export const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
export const AUTH_API = import.meta.env.VITE_AUTH_API ?? "http://localhost:8088/api/auth";
export const AUTH_BYPASS = (import.meta.env.VITE_AUTH_BYPASS ?? "false") === "true";