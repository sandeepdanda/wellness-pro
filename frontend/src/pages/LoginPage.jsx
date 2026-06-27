import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { errorMessage } from '../services/api.js';
import { ErrorNote } from '../components/common/ui.jsx';

export default function LoginPage() {
  const { isAuthenticated, login, register } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      if (mode === 'login') {
        await login({ email: form.email, password: form.password });
      } else {
        await register(form);
      }
      navigate('/dashboard');
    } catch (err) {
      setError(errorMessage(err, 'Could not sign you in'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-sage-100 via-sand to-clay-50 px-6">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="text-5xl">🌿</div>
          <h1 className="mt-3 text-3xl font-bold text-sage-800">Wellness Pro</h1>
          <p className="mt-1 text-sage-500">Breathe in. Book your next session.</p>
        </div>

        <form onSubmit={submit} className="card space-y-4 p-8">
          {mode === 'register' && (
            <div>
              <label className="label">Name</label>
              <input className="input" value={form.name} onChange={update('name')} required />
            </div>
          )}
          <div>
            <label className="label">Email</label>
            <input className="input" type="email" value={form.email} onChange={update('email')} required />
          </div>
          <div>
            <label className="label">Password</label>
            <input className="input" type="password" value={form.password} onChange={update('password')} required />
          </div>
          {mode === 'register' && (
            <div>
              <label className="label">Phone (optional)</label>
              <input className="input" value={form.phone} onChange={update('phone')} />
            </div>
          )}

          {error && <ErrorNote>{error}</ErrorNote>}

          <button type="submit" className="btn-primary w-full" disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
          </button>

          <p className="text-center text-sm text-sage-500">
            {mode === 'login' ? "New here? " : 'Already a member? '}
            <button
              type="button"
              className="font-semibold text-sage-700 hover:underline"
              onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}
            >
              {mode === 'login' ? 'Create an account' : 'Sign in'}
            </button>
          </p>
        </form>

        {mode === 'login' && (
          <p className="mt-4 text-center text-xs text-sage-400">
            Demo: admin@wellnesspro.dev / member@wellnesspro.dev · password123
          </p>
        )}
      </div>
    </div>
  );
}
