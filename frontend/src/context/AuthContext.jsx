import { createContext, useContext, useMemo, useState } from 'react';
import { login as loginApi, register as registerApi } from '../services/api';

const AuthContext = createContext(null);

// Stored auth response: { token, memberId, name, email, role }.
function readStoredAuth() {
  const token = localStorage.getItem('token');
  const raw = localStorage.getItem('auth');
  if (!token || !raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth);

  const persist = (data) => {
    localStorage.setItem('token', data.token);
    localStorage.setItem('auth', JSON.stringify(data));
    setAuth(data);
  };

  const value = useMemo(
    () => ({
      auth,
      isAuthenticated: !!auth,
      isAdmin: auth?.role === 'ADMIN',
      async login(credentials) {
        const data = await loginApi(credentials);
        persist(data);
        return data;
      },
      async register(payload) {
        const data = await registerApi(payload);
        persist(data);
        return data;
      },
      logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('auth');
        setAuth(null);
      },
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
