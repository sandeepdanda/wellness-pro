import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getPlans, getMyProfile, subscribe, errorMessage } from '../services/api.js';
import { Spinner, ErrorNote, Badge } from '../components/common/ui.jsx';

const currency = new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' });

export default function PlansPage() {
  const queryClient = useQueryClient();
  const [method, setMethod] = useState('CARD');
  const [toast, setToast] = useState(null);

  const plansQuery = useQuery({ queryKey: ['plans'], queryFn: getPlans });
  const profileQuery = useQuery({ queryKey: ['profile'], queryFn: getMyProfile });

  const subscribeMutation = useMutation({
    mutationFn: (planId) => subscribe(planId, method),
    onSuccess: (member) => {
      setToast({ tone: 'sage', text: 'Subscribed! Your plan is active.' });
      queryClient.setQueryData(['profile'], member);
      queryClient.invalidateQueries({ queryKey: ['payments'] });
    },
    onError: (err) => setToast({ tone: 'clay', text: errorMessage(err, 'Could not subscribe') }),
  });

  if (plansQuery.isLoading) return <Spinner label="Loading plans…" />;
  if (plansQuery.isError) return <ErrorNote>Could not load plans.</ErrorNote>;

  const currentPlanId = profileQuery.data?.membershipPlanId;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-sage-800">Membership plans</h1>
          <p className="text-sage-500">Pick the rhythm that fits your practice.</p>
        </div>
        <div>
          <label className="label">Payment method</label>
          <select className="input w-52" value={method} onChange={(e) => setMethod(e.target.value)}>
            <option value="CARD">Card</option>
            <option value="CASH">Cash</option>
            <option value="BANK_TRANSFER">Bank transfer</option>
          </select>
        </div>
      </div>

      {toast && (
        <div className={toast.tone === 'clay'
          ? 'rounded-2xl border border-clay-200 bg-clay-50 px-4 py-3 text-sm text-clay-700'
          : 'rounded-2xl border border-sage-200 bg-sage-50 px-4 py-3 text-sm text-sage-700'}>
          {toast.text}
        </div>
      )}

      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {(plansQuery.data ?? []).map((p) => {
          const current = p.id === currentPlanId;
          const perMonth = p.durationMonths > 0 ? p.price / p.durationMonths : p.price;
          return (
            <div key={p.id} className={`card flex flex-col gap-3 p-6 ${current ? 'ring-2 ring-sage-400' : ''}`}>
              <div className="flex items-start justify-between">
                <h2 className="text-lg font-semibold text-sage-800">{p.name}</h2>
                {current && <Badge>Current</Badge>}
              </div>
              <p className="text-3xl font-bold text-sage-700">
                {currency.format(p.price)}
                <span className="text-sm font-normal text-sage-400"> / {p.durationMonths} mo</span>
              </p>
              <p className="text-sm text-sage-500">{currency.format(perMonth)} per month</p>
              <ul className="mt-1 space-y-1 text-sm text-sage-600">
                {(p.features ?? '').split(',').map((f, i) => (
                  <li key={i} className="flex gap-2"><span className="text-sage-400">✓</span>{f.trim()}</li>
                ))}
              </ul>
              <button
                className="btn-primary mt-auto"
                disabled={current || subscribeMutation.isPending}
                onClick={() => subscribeMutation.mutate(p.id)}
              >
                {current ? 'Your plan' : 'Subscribe'}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
