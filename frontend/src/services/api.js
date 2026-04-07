import axios from 'axios';

const API = axios.create({ baseURL: '/api' });

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const login = (credentials) => API.post('/auth/login', credentials);
export const register = (data) => API.post('/auth/register', data);
export const getMembers = () => API.get('/members');
export const getMember = (id) => API.get(`/members/${id}`);
export const getClasses = () => API.get('/classes');
export const getClassesByLocation = (locationId) => API.get(`/classes/location/${locationId}`);
export const getLocations = () => API.get('/locations');
export const createBooking = (data) => API.post('/bookings', data);
export const getBookings = (memberId) => API.get(`/bookings/member/${memberId}`);
export const cancelBooking = (id) => API.patch(`/bookings/${id}/cancel`);

export default API;
