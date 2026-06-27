import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';

const navItem = ({ isActive }) =>
  `px-4 py-2 rounded-full text-sm font-medium transition ${
    isActive ? 'bg-sage-600 text-white' : 'text-sage-700 hover:bg-sage-100'
  }`;

export default function Layout({ children }) {
  const { auth, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-sand to-sage-50">
      <header className="sticky top-0 z-10 border-b border-sage-100 bg-sand/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <NavLink to="/dashboard" className="flex items-center gap-2 text-lg font-bold text-sage-800">
            <span className="text-2xl">🌿</span> Wellness Pro
          </NavLink>
          <nav className="hidden items-center gap-1 md:flex">
            <NavLink to="/dashboard" className={navItem}>Dashboard</NavLink>
            <NavLink to="/classes" className={navItem}>Classes</NavLink>
            <NavLink to="/bookings" className={navItem}>Bookings</NavLink>
            <NavLink to="/plans" className={navItem}>Plans</NavLink>
            <NavLink to="/profile" className={navItem}>Profile</NavLink>
            {isAdmin && <NavLink to="/admin" className={navItem}>Admin</NavLink>}
            {isAdmin && <NavLink to="/admin/classes" className={navItem}>Classes Mgmt</NavLink>}
          </nav>
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-sage-600 sm:inline">{auth?.name}</span>
            <button onClick={handleLogout} className="btn-ghost">Sign out</button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">{children}</main>
    </div>
  );
}
