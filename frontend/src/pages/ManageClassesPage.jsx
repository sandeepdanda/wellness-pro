import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getClasses, getLocations, createClass, deleteClass, errorMessage } from '../services/api.js';
import { Spinner, ErrorNote, Badge } from '../components/common/ui.jsx';

function formatSchedule(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString(undefined, {
    weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
  });
}

const emptyForm = { name: '', instructor: '', locationId: '', schedule: '', maxCapacity: 20 };

export default function ManageClassesPage() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: getClasses });
  const locationsQuery = useQuery({ queryKey: ['locations'], queryFn: getLocations });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['classes'] });

  const createMutation = useMutation({
    mutationFn: () => createClass({
      name: form.name,
      instructor: form.instructor,
      locationId: Number(form.locationId),
      schedule: form.schedule,
      maxCapacity: Number(form.maxCapacity),
    }),
    onSuccess: () => { setForm(emptyForm); setError(''); invalidate(); },
    onError: (err) => setError(errorMessage(err, 'Could not create class')),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => deleteClass(id),
    onSuccess: invalidate,
    onError: (err) => setError(errorMessage(err, 'Could not delete class')),
  });

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-sage-800">Manage classes</h1>
        <p className="text-sage-500">Add new sessions or remove ones that are done.</p>
      </div>

      <form
        className="card grid gap-4 p-6 sm:grid-cols-2 lg:grid-cols-3"
        onSubmit={(e) => { e.preventDefault(); createMutation.mutate(); }}
      >
        <div>
          <label className="label">Class name</label>
          <input className="input" value={form.name} onChange={update('name')} required />
        </div>
        <div>
          <label className="label">Instructor</label>
          <input className="input" value={form.instructor} onChange={update('instructor')} required />
        </div>
        <div>
          <label className="label">Location</label>
          <select className="input" value={form.locationId} onChange={update('locationId')} required>
            <option value="">Select…</option>
            {(locationsQuery.data ?? []).map((l) => (
              <option key={l.id} value={l.id}>{l.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label">Schedule</label>
          <input className="input" type="datetime-local" value={form.schedule} onChange={update('schedule')} required />
        </div>
        <div>
          <label className="label">Max capacity</label>
          <input className="input" type="number" min="1" value={form.maxCapacity} onChange={update('maxCapacity')} required />
        </div>
        <div className="flex items-end">
          <button type="submit" className="btn-primary w-full" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Adding…' : 'Add class'}
          </button>
        </div>
        {error && <div className="sm:col-span-2 lg:col-span-3"><ErrorNote>{error}</ErrorNote></div>}
      </form>

      {classesQuery.isLoading ? (
        <Spinner label="Loading classes…" />
      ) : (
        <div className="space-y-3">
          {(classesQuery.data ?? []).map((c) => (
            <div key={c.id} className="card flex flex-wrap items-center justify-between gap-4 p-5">
              <div>
                <div className="flex items-center gap-3">
                  <h2 className="font-semibold text-sage-800">{c.name}</h2>
                  <Badge tone={c.availableSpots <= 0 ? 'clay' : 'sage'}>
                    {c.currentEnrollment}/{c.maxCapacity}
                  </Badge>
                </div>
                <p className="text-sm text-sage-500">{c.instructor} · {c.locationName}</p>
                <p className="text-sm text-sage-600">{formatSchedule(c.schedule)}</p>
              </div>
              <button
                className="btn-danger"
                disabled={deleteMutation.isPending}
                onClick={() => deleteMutation.mutate(c.id)}
              >
                Delete
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
