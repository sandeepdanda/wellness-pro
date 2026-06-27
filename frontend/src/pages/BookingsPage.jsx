import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMyBookings, cancelBooking, errorMessage } from '../services/api.js';
import { Spinner, ErrorNote, EmptyState, Badge } from '../components/common/ui.jsx';

function formatSchedule(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString(undefined, {
    weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
  });
}

const statusTone = { CONFIRMED: 'sage', CANCELLED: 'clay', COMPLETED: 'muted' };

export default function BookingsPage() {
  const queryClient = useQueryClient();
  const bookingsQuery = useQuery({ queryKey: ['bookings'], queryFn: getMyBookings });

  const cancelMutation = useMutation({
    mutationFn: (id) => cancelBooking(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
      queryClient.invalidateQueries({ queryKey: ['classes'] });
    },
  });

  if (bookingsQuery.isLoading) return <Spinner label="Loading your bookings…" />;
  if (bookingsQuery.isError) return <ErrorNote>Could not load bookings.</ErrorNote>;

  const bookings = bookingsQuery.data ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-sage-800">My bookings</h1>
        <p className="text-sage-500">Everything you have lined up.</p>
      </div>

      {cancelMutation.isError && (
        <ErrorNote>{errorMessage(cancelMutation.error, 'Could not cancel that booking')}</ErrorNote>
      )}

      {bookings.length === 0 ? (
        <EmptyState title="No bookings yet" hint="Head to Classes to book your first session." />
      ) : (
        <div className="space-y-3">
          {bookings.map((b) => (
            <div key={b.id} className="card flex flex-wrap items-center justify-between gap-4 p-5">
              <div>
                <div className="flex items-center gap-3">
                  <h2 className="font-semibold text-sage-800">{b.className}</h2>
                  <Badge tone={statusTone[b.status] ?? 'muted'}>{b.status.toLowerCase()}</Badge>
                </div>
                <p className="text-sm text-sage-500">with {b.instructor}</p>
                <p className="text-sm text-sage-600">{formatSchedule(b.classSchedule)}</p>
              </div>
              {b.status === 'CONFIRMED' && (
                <button
                  className="btn-danger"
                  disabled={cancelMutation.isPending}
                  onClick={() => cancelMutation.mutate(b.id)}
                >
                  Cancel
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
