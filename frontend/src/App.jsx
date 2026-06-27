import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/common/ProtectedRoute.jsx';
import Layout from './components/common/Layout.jsx';
import LoginPage from './pages/LoginPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import ClassesPage from './pages/ClassesPage.jsx';
import BookingsPage from './pages/BookingsPage.jsx';
import PlansPage from './pages/PlansPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import AdminPage from './pages/AdminPage.jsx';
import ManageClassesPage from './pages/ManageClassesPage.jsx';

const withLayout = (element) => (
  <ProtectedRoute>
    <Layout>{element}</Layout>
  </ProtectedRoute>
);

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/dashboard" element={withLayout(<DashboardPage />)} />
      <Route path="/classes" element={withLayout(<ClassesPage />)} />
      <Route path="/bookings" element={withLayout(<BookingsPage />)} />
      <Route path="/plans" element={withLayout(<PlansPage />)} />
      <Route path="/profile" element={withLayout(<ProfilePage />)} />
      <Route
        path="/admin"
        element={
          <ProtectedRoute adminOnly>
            <Layout><AdminPage /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/classes"
        element={
          <ProtectedRoute adminOnly>
            <Layout><ManageClassesPage /></Layout>
          </ProtectedRoute>
        }
      />
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
