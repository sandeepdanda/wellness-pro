import React, { useEffect, useState } from 'react';
import { getClasses, createBooking } from '../services/api';

export default function ClassesPage() {
  const [classes, setClasses] = useState([]);

  useEffect(() => { getClasses().then(res => setClasses(res.data)); }, []);

  const handleBook = async (classId) => {
    // TODO: Get current member ID from auth context
    await createBooking({ fitnessClass: { id: classId } });
    alert('Booked!');
  };

  return (
    <div className="classes-page">
      <h1>Available Classes</h1>
      <div className="class-grid">
        {classes.map(c => (
          <div key={c.id} className="class-card">
            <h3>{c.name}</h3>
            <p>Instructor: {c.instructor}</p>
            <p>Capacity: {c.currentEnrollment}/{c.maxCapacity}</p>
            <button onClick={() => handleBook(c.id)} disabled={c.currentEnrollment >= c.maxCapacity}>
              {c.currentEnrollment >= c.maxCapacity ? 'Full' : 'Book Now'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
