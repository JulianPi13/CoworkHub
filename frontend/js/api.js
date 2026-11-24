// Cliente HTTP de la API de CoWorkHub. Guarda el JWT en sessionStorage y lo envía en cada petición.
const Api = (() => {
  const BASE = window.COWORKHUB_CONFIG.API_URL;
  const KEY = 'coworkhub.session';

  class ApiError extends Error {
    constructor(status, body) {
      super(body?.error || `Error HTTP ${status}`);
      this.status = status;
      this.codigo = body?.codigo || String(status);
    }
  }

  function session() {
    try { return JSON.parse(sessionStorage.getItem(KEY)); } catch { return null; }
  }

  function saveSession(data) {
    try { sessionStorage.setItem(KEY, JSON.stringify(data)); } catch { /* almacenamiento no disponible */ }
  }

  function clearSession() {
    try { sessionStorage.removeItem(KEY); } catch { /* nada */ }
  }

  async function request(method, path, body) {
    const headers = { 'Content-Type': 'application/json' };
    const s = session();
    if (s?.token) headers.Authorization = `Bearer ${s.token}`;

    let res;
    try {
      res = await fetch(BASE + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
    } catch {
      throw new ApiError(0, { error: `No se pudo conectar con la API en ${BASE}. ¿Está corriendo el backend?`, codigo: 'SIN_CONEXION' });
    }
    const text = await res.text();
    const data = text ? JSON.parse(text) : null;
    if (!res.ok) {
      if (res.status === 401 && path !== '/auth/login') {
        clearSession();
        window.dispatchEvent(new Event('coworkhub:logout'));
      }
      throw new ApiError(res.status, data);
    }
    return data;
  }

  const qs = (params) => {
    const p = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v === undefined || v === null || v === '') return;
      if (Array.isArray(v)) { if (v.length) p.set(k, v.join(',')); } else p.set(k, v);
    });
    const s = p.toString();
    return s ? `?${s}` : '';
  };

  return {
    ApiError, session, saveSession, clearSession, qs,
    get: (path) => request('GET', path),
    post: (path, body) => request('POST', path, body),
    put: (path, body) => request('PUT', path, body),
    patch: (path, body) => request('PATCH', path, body),
    del: (path) => request('DELETE', path),
  };
})();
