export type JwtPayload = { exp?: number; [k: string]: unknown };

export function decodeJwt(token?: string | null): JwtPayload | null {
  if (!token) return null;
  try {
    const [, payloadB64] = token.split(".");
    const json = atob(payloadB64.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json);
  } catch {
    return null;
  }
}