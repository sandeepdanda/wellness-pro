import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getMyProfile, getMyBookings, getClasses } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';
import { Spinner, Badge } from '../components/common/ui.jsx';

function StatCard({ label, value, accent = 'sage' }) {
  const ring = accent === 'clay' ? 'text-clay-600' : 'text-sage-600';
  return (
    <div className="card p-6">
      <p className="text-sm text-sage-500">{label}</p>
      <p className={`mt-2 text-4xl font-bold ${ring}`}>{value}</p>
    </div>
  );
}

function formatSchedule(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString(undefined, {
    weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
  });
}

export default function DashboardPage() {
  const { auth } = useAuth();
  const profileQuery = useQuery({ queryKey: ['profile'], queryFn: getMyProfile });
  const bookingsQuery = useQuery({ queryKey: ['bookings'], queryFn: getMyBookings });
  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: getClasses });

  if (bookingsQuery.isLoading || profileQuery.isLoading) return <Spinner />;

  const bookings = bookingsQuery.data ?? [];
  const confirmed = bookings.filter((b) => b.status === 'CONFIRMED');
  const upcoming = confirmed
    .filter((b) => b.classSchedule && new Date(b.classSchedule) > new Date())
    .sort((a, b) => new Date(a.classSchedule) - new Date(b.classSchedule));
  const next = upcoming[0];
  const openClasses = (classesQuery.data ?? []).filter((c) => c.availableSpots > 0).length;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-sage-800">
          Welcome back, {auth?.name?.split(' ')[0] ?? 'friend'} 🌅
        </h1>
        <p className="text-sage-500">Here is your week at a glance.</p>
      </div>

      <div className="grid gap-5 sm:grid-cols-3">
        <StatCard label="Upcoming sessions" value={upcoming.length} />
        <StatCard label="Total bookings" value={bookings.length} />
        <StatCard label="Open classes now" value={openClasses} accent="clay" />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="card p-6">
          <h2 className="mb-4 text-lg font-semibold text-sage-800">Your next session</h2>
          {next ? (
            <div className="space-y-2">
              <div className="flex items-center gap-3">
                <span className="text-xl font-semibold text-sage-800">{next.className}</span>
                <Badge>confirmed</Badge>
              </div>
              <p className="text-sage-500">with {next.instructor}</p>
              <p className="font-medium text-sage-700">{formatSchedule(next.classSchedule)}</p>
              <Link to="/bookings" className="btn-ghost mt-2 inline-flex">View all bookings →</Link>
            </div>
          ) : (
            <div className="space-y-3">
              <p className="text-sage-500">Nothing booked yet. Your mat is waiting.</p>
              <Link to="/classes" className="btn-primary inline-flex">Browse classes</Link>
            </div>
          )}
        </div>

        <div className="card p-6">
          <h2 className="mb-4 text-lg font-semibold text-sage-800">Membership</h2>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between"><dt className="text-sage-500">Name</dt><dd className="font-medium text-sage-800">{profileQuery.data?.name}</dd></div>
            <div className="flex justify-between"><dt className="text-sage-500">Email</dt><dd className="font-medium text-sage-800">{profileQuery.data?.email}</dd></div>
            <div className="flex justify-between"><dt className="text-sage-500">Status</dt><dd><Badge>{(profileQuery.data?.status ?? '').toLowerCase()}</Badge></dd></div>
            <div className="flex justify-between"><dt className="text-sage-500">Member since</dt><dd className="font-medium text-sage-800">{profileQuery.data?.joinDate}</dd></div>
          </dl>
        </div>
      </div>
    </div>
  );
}
