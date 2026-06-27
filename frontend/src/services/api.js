import axios from 'axios';

const API = axios.create({ baseURL: '/api' });

// Attach the JWT on every request.
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// On 401, drop the stale token so the app redirects to login.
API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
    }
    return Promise.reject(error);
  }
);

// Auth
export const login = (credentials) => API.post('/auth/login', credentials).then((r) => r.data);
export const register = (data) => API.post('/auth/register', data).then((r) => r.data);

// Catalog
export const getClasses = () => API.get('/classes').then((r) => r.data);
export const getLocations = () => API.get('/locations').then((r) => r.data);
export const getPlans = () => API.get('/plans').then((r) => r.data);

// Member
export const getMyProfile = () => API.get('/members/me').then((r) => r.data);
export const updateMyProfile = (data) => API.patch('/members/me', data).then((r) => r.data);

// Bookings
export const getMyBookings = () => API.get('/bookings/me').then((r) => r.data);
export const createBooking = (classId) => API.post('/bookings', { classId }).then((r) => r.data);
export const cancelBooking = (id) => API.patch(`/bookings/${id}/cancel`).then((r) => r.data);

// Payments + subscription
export const getMyPayments = () => API.get('/payments/me').then((r) => r.data);
export const subscribe = (planId, method) => API.post('/payments/subscribe', { planId, method }).then((r) => r.data);

// Admin
export const getAnalytics = () => API.get('/admin/analytics').then((r) => r.data);
export const getAllMembers = () => API.get('/admin/members').then((r) => r.data);
export const createClass = (data) => API.post('/classes', data).then((r) => r.data);
export const updateClass = (id, data) => API.put(`/classes/${id}`, data).then((r) => r.data);
export const deleteClass = (id) => API.delete(`/classes/${id}`).then((r) => r.data);

// Helper for extracting a readable error message from an Axios error.
export const errorMessage = (error, fallback = 'Something went wrong') =>
  error?.response?.data?.message || fallback;

export default API;
