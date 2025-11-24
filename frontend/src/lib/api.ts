export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

let unauthorizedHandler: (() => void) | null = null;
export function onUnauthorized(handler: () => void) { unauthorizedHandler = handler; }

export async function api<T>(
  base: string,
  path: string,
  method: HttpMethod = "GET",
  body?: any,
  token?: string
): Promise<T> {
  const res = await fetch(`${base}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    if (res.status === 401) {
      unauthorizedHandler?.();
    }
    let msg = `HTTP ${res.status}`;
    try {
      const data = await res.json();
      msg = data?.message || data?.error || msg;
    } catch { }
    throw new Error(msg);
  }
  return res.status === 204 ? (undefined as unknown as T) : await res.json();
}