import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getClasses, getLocations, createBooking, errorMessage } from '../services/api.js';
import { Spinner, ErrorNote, EmptyState, Badge } from '../components/common/ui.jsx';

function formatSchedule(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString(undefined, {
    weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
  });
}

export default function ClassesPage() {
  const queryClient = useQueryClient();
  const [locationFilter, setLocationFilter] = useState('all');
  const [toast, setToast] = useState(null);

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: getClasses });
  const locationsQuery = useQuery({ queryKey: ['locations'], queryFn: getLocations });

  const bookMutation = useMutation({
    mutationFn: (classId) => createBooking(classId),
    onSuccess: () => {
      setToast({ tone: 'sage', text: 'Booked! See it under My Bookings.' });
      queryClient.invalidateQueries({ queryKey: ['classes'] });
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    },
    onError: (err) => setToast({ tone: 'clay', text: errorMessage(err, 'Could not book that class') }),
  });

  if (classesQuery.isLoading) return <Spinner label="Loading classes…" />;
  if (classesQuery.isError) return <ErrorNote>Could not load classes.</ErrorNote>;

  const classes = classesQuery.data ?? [];
  const filtered = locationFilter === 'all'
    ? classes
    : classes.filter((c) => String(c.locationId) === locationFilter);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-sage-800">Browse classes</h1>
          <p className="text-sage-500">Find a session that fits your flow.</p>
        </div>
        <div>
          <label className="label">Location</label>
          <select className="input w-56" value={locationFilter} onChange={(e) => setLocationFilter(e.target.value)}>
            <option value="all">All locations</option>
            {(locationsQuery.data ?? []).map((l) => (
              <option key={l.id} value={String(l.id)}>{l.name}</option>
            ))}
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

      {filtered.length === 0 ? (
        <EmptyState title="No classes here yet" hint="Try a different location." />
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((c) => {
            const full = c.availableSpots <= 0;
            return (
              <div key={c.id} className="card flex flex-col gap-3 p-6">
                <div className="flex items-start justify-between">
                  <h2 className="text-lg font-semibold text-sage-800">{c.name}</h2>
                  {full ? <Badge tone="clay">Full</Badge> : <Badge>{c.availableSpots} left</Badge>}
                </div>
                <p className="text-sm text-sage-500">with {c.instructor}</p>
                <p className="text-sm text-sage-600">{c.locationName}</p>
                <p className="text-sm font-medium text-sage-700">{formatSchedule(c.schedule)}</p>
                <div className="mt-1 h-2 w-full overflow-hidden rounded-full bg-sage-100">
                  <div
                    className="h-full rounded-full bg-sage-500"
                    style={{ width: `${Math.min(100, (c.currentEnrollment / c.maxCapacity) * 100)}%` }}
                  />
                </div>
                <button
                  className="btn-primary mt-2"
                  disabled={full || bookMutation.isPending}
                  onClick={() => bookMutation.mutate(c.id)}
                >
                  {full ? 'Class full' : 'Book this class'}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
