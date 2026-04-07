import React, { useEffect, useState } from 'react';
import { getBookings, cancelBooking } from '../services/api';

export default function BookingsPage() {
  const [bookings, setBookings] = useState([]);

  // TODO: Get member ID from auth context
  useEffect(() => { getBookings(1).then(res => setBookings(res.data)); }, []);

  const handleCancel = async (id) => {
    await cancelBooking(id);
    setBookings(prev => prev.map(b => b.id === id ? { ...b, status: 'CANCELLED' } : b));
  };

  return (
    <div className="bookings-page">
      <h1>My Bookings</h1>
      {bookings.map(b => (
        <div key={b.id} className="booking-card">
          <h3>{b.fitnessClass?.name}</h3>
          <p>Status: {b.status}</p>
          {b.status === 'CONFIRMED' && <button onClick={() => handleCancel(b.id)}>Cancel</button>}
        </div>
      ))}
    </div>
  );
}
