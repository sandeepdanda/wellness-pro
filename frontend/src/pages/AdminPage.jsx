import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getAnalytics, getAllMembers } from '../services/api.js';
import { Spinner, ErrorNote, Badge } from '../components/common/ui.jsx';

function StatCard({ label, value }) {
  return (
    <div className="card p-6">
      <p className="text-sm text-sage-500">{label}</p>
      <p className="mt-2 text-3xl font-bold text-sage-700">{value}</p>
    </div>
  );
}

function Bar({ label, sub, pct }) {
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-sm">
        <span className="font-medium text-sage-700">{label}</span>
        <span className="text-sage-500">{sub}</span>
      </div>
      <div className="h-3 w-full overflow-hidden rounded-full bg-sage-100">
        <div className="h-full rounded-full bg-sage-500" style={{ width: `${Math.min(100, pct)}%` }} />
      </div>
    </div>
  );
}

export default function AdminPage() {
  const analyticsQuery = useQuery({ queryKey: ['analytics'], queryFn: getAnalytics });
  const membersQuery = useQuery({ queryKey: ['admin-members'], queryFn: getAllMembers });

  if (analyticsQuery.isLoading) return <Spinner label="Crunching numbers…" />;
  if (analyticsQuery.isError) return <ErrorNote>Could not load analytics.</ErrorNote>;

  const a = analyticsQuery.data;
  const currency = new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' });

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-3xl font-bold text-sage-800">Admin dashboard</h1>
          <p className="text-sage-500">Revenue, occupancy, and membership health.</p>
        </div>
        <Link to="/admin/classes" className="btn-primary">Manage classes</Link>
      </div>

      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-5">
        <StatCard label="Total members" value={a.totalMembers} />
        <StatCard label="Active members" value={a.activeMembers} />
        <StatCard label="Classes" value={a.totalClasses} />
        <StatCard label="Confirmed bookings" value={a.confirmedBookings} />
        <StatCard label="Revenue" value={currency.format(a.totalRevenue)} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="card p-6">
          <h2 className="mb-4 text-lg font-semibold text-sage-800">
            Occupancy by location
            <span className="ml-2 text-sm font-normal text-sage-500">avg {a.averageOccupancyPct}%</span>
          </h2>
          <div className="space-y-4">
            {a.occupancyByLocation.map((l) => (
              <Bar key={l.locationId} label={l.locationName} sub={`${l.booked}/${l.totalCapacity} · ${l.occupancyPct}%`} pct={l.occupancyPct} />
            ))}
          </div>
        </div>

        <div className="card p-6">
          <h2 className="mb-4 text-lg font-semibold text-sage-800">Top classes by fill</h2>
          <div className="space-y-4">
            {a.topClasses.map((c) => (
              <Bar key={c.classId} label={`${c.className} · ${c.instructor}`} sub={`${c.currentEnrollment}/${c.maxCapacity} · ${c.fillPct}%`} pct={c.fillPct} />
            ))}
          </div>
        </div>
      </div>

      <div className="card overflow-hidden">
        <h2 className="border-b border-sage-100 px-6 py-4 text-lg font-semibold text-sage-800">Members</h2>
        {membersQuery.isLoading ? (
          <Spinner label="Loading members…" />
        ) : (
          <table className="w-full text-left text-sm">
            <thead className="bg-sage-50 text-sage-500">
              <tr>
                <th className="px-6 py-3 font-medium">Name</th>
                <th className="px-6 py-3 font-medium">Email</th>
                <th className="px-6 py-3 font-medium">Role</th>
                <th className="px-6 py-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {(membersQuery.data ?? []).map((m) => (
                <tr key={m.id} className="border-t border-sage-50">
                  <td className="px-6 py-3 font-medium text-sage-800">{m.name}</td>
                  <td className="px-6 py-3 text-sage-600">{m.email}</td>
                  <td className="px-6 py-3"><Badge tone={m.role === 'ADMIN' ? 'clay' : 'sage'}>{m.role.toLowerCase()}</Badge></td>
                  <td className="px-6 py-3 text-sage-600">{m.status.toLowerCase()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
