export type SseEvent = { type?: string; data: any };

type Listener = (ev: SseEvent) => void;

class SseClient {
  private controller?: AbortController;
  private listeners = new Set<Listener>();
  private active = false;

  connect(url: string, token: string) {
    if (this.active) {
      console.debug("[SSE] Ya hay una conexión activa, no se vuelve a conectar");
      return;
    }
    console.debug("[SSE] Iniciando conexión:", url);
    this.active = true;
    this.controller = new AbortController();
    this.run(url, token, this.controller.signal);
  }

  private async run(url: string, token: string, signal: AbortSignal) {
    try {
      const res = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "text/event-stream"
        },
        signal,
        cache: "no-store"
      });

      console.debug("[SSE] Respuesta recibida:", res.status, res.statusText);

      if (!res.ok || !res.body) {
        console.error("[SSE] Respuesta no OK o sin body:", res.status, res.statusText);
        this.active = false;
        return;
      }

      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      while (true) {
        const { value, done } = await reader.read();
        if (done) {
          console.debug("[SSE] Stream terminado por el servidor");
          break;
        }
        buffer += decoder.decode(value, { stream: true });

        while (true) {
          const idxLF = buffer.indexOf("\n\n");
          const idxCRLF = buffer.indexOf("\r\n\r\n");
          let idx = -1, sep = 2;

          if (idxLF === -1 && idxCRLF === -1) break;
          if (idxLF !== -1 && (idxCRLF === -1 || idxLF < idxCRLF)) {
            idx = idxLF;
            sep = 2;
          } else {
            idx = idxCRLF;
            sep = 4;
          }

          const block = buffer.slice(0, idx);
          buffer = buffer.slice(idx + sep);

          let type: string | undefined;
          const datas: string[] = [];

          for (const l of block.split(/\r?\n/)) {
            if (l.startsWith("event:")) type = l.slice(6).trim();
            else if (l.startsWith("data:")) datas.push(l.slice(5).trim());
          }

          const dataStr = datas.join("\n");
          let data: any = dataStr;
          try { data = JSON.parse(dataStr); } catch {}

          console.debug("[SSE] Evento recibido:", { type, data });

          this.listeners.forEach(fn => fn({ type, data }));
        }
      }
    } catch (e: any) {
      if (signal.aborted) {
        console.debug("[SSE] Conexión abortada (disconnect)");
      } else {
        console.error("[SSE] Error en la conexión SSE:", e);
      }
    } finally {
      this.active = false;
      console.debug("[SSE] Estado activo = false");
    }
  }

  on(fn: Listener) {
    this.listeners.add(fn);
    return () => this.listeners.delete(fn);
  }

  disconnect() {
    if (this.controller) {
      console.debug("[SSE] Abortando conexión");
      this.controller.abort();
    }
    this.active = false;
  }
}

export const sseClient = (globalThis as any).__SSE_CLIENT__ ?? new SseClient();
(globalThis as any).__SSE_CLIENT__ = sseClient;
