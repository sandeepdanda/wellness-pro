import React from 'react';
// TODO: Fetch member stats, upcoming bookings, membership status

export default function DashboardPage() {
  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Membership</h3>
          <p>Active — Annual Premium</p>
        </div>
        <div className="stat-card">
          <h3>Upcoming Classes</h3>
          <p>3 this week</p>
        </div>
        <div className="stat-card">
          <h3>Location</h3>
          <p>Downtown Branch</p>
        </div>
      </div>
      {/* TODO: Recent activity feed, payment history */}
    </div>
  );
}
