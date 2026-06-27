import { useEffect, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMyProfile, updateMyProfile, getMyPayments, errorMessage } from '../services/api.js';
import { Spinner, ErrorNote, Badge } from '../components/common/ui.jsx';

const currency = new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' });

function formatDate(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
}

export default function ProfilePage() {
  const queryClient = useQueryClient();
  const profileQuery = useQuery({ queryKey: ['profile'], queryFn: getMyProfile });
  const paymentsQuery = useQuery({ queryKey: ['payments'], queryFn: getMyPayments });

  const [form, setForm] = useState({ name: '', phone: '' });
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (profileQuery.data) {
      setForm({ name: profileQuery.data.name ?? '', phone: profileQuery.data.phone ?? '' });
    }
  }, [profileQuery.data]);

  const updateMutation = useMutation({
    mutationFn: () => updateMyProfile(form),
    onSuccess: (member) => {
      queryClient.setQueryData(['profile'], member);
      setSaved(true);
      setTimeout(() => setSaved(false), 2500);
    },
  });

  if (profileQuery.isLoading) return <Spinner label="Loading profile…" />;

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-sage-800">Your profile</h1>
        <p className="text-sage-500">Update your details and review your payments.</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <form
          className="card space-y-4 p-6"
          onSubmit={(e) => { e.preventDefault(); updateMutation.mutate(); }}
        >
          <h2 className="text-lg font-semibold text-sage-800">Details</h2>
          <div>
            <label className="label">Name</label>
            <input className="input" value={form.name} onChange={update('name')} required />
          </div>
          <div>
            <label className="label">Phone</label>
            <input className="input" value={form.phone} onChange={update('phone')} />
          </div>
          <div>
            <label className="label">Email</label>
            <input className="input bg-sage-50 text-sage-400" value={profileQuery.data?.email ?? ''} disabled />
          </div>
          {updateMutation.isError && (
            <ErrorNote>{errorMessage(updateMutation.error, 'Could not save')}</ErrorNote>
          )}
          <div className="flex items-center gap-3">
            <button type="submit" className="btn-primary" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? 'Saving…' : 'Save changes'}
            </button>
            {saved && <span className="text-sm text-sage-600">Saved ✓</span>}
          </div>
        </form>

        <div className="card p-6">
          <h2 className="mb-4 text-lg font-semibold text-sage-800">Payment history</h2>
          {paymentsQuery.isLoading ? (
            <Spinner label="Loading…" />
          ) : (paymentsQuery.data ?? []).length === 0 ? (
            <p className="text-sm text-sage-500">No payments yet. Subscribe to a plan to get started.</p>
          ) : (
            <ul className="divide-y divide-sage-100">
              {paymentsQuery.data.map((p) => (
                <li key={p.id} className="flex items-center justify-between py-3">
                  <div>
                    <p className="font-medium text-sage-800">{p.description ?? 'Payment'}</p>
                    <p className="text-xs text-sage-500">{formatDate(p.paymentDate)} · {p.method}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-sage-700">{currency.format(p.amount)}</p>
                    <Badge tone={p.status === 'COMPLETED' ? 'sage' : 'clay'}>{p.status.toLowerCase()}</Badge>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
