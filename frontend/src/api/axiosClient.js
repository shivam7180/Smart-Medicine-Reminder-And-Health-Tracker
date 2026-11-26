import axios from 'axios';
import { handleMockRoute } from './mockDataStore';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 1500,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Bearer Token if present
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('medicare_access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Seamlessly fall back if Gateway is offline
axiosClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config;

    // 1. Handle Token Refresh on HTTP 401
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('medicare_refresh_token');

      if (refreshToken) {
        try {
          const res = await axios.post(`${API_BASE_URL}/api/v1/auth/refresh-token`, {
            refreshToken,
          });

          if (res.data?.success && res.data.data?.accessToken) {
            const newAccessToken = res.data.data.accessToken;
            localStorage.setItem('medicare_access_token', newAccessToken);
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return axiosClient(originalRequest);
          }
        } catch (refreshErr) {
          localStorage.removeItem('medicare_access_token');
          localStorage.removeItem('medicare_refresh_token');
          localStorage.removeItem('medicare_user');
          window.location.href = '/login';
        }
      }
    }

    // 2. Seamless Offline Dev Fallback for Network Errors (when Port 8080 is offline)
    if (
      !error.response ||
      error.message === 'Network Error' ||
      error.code === 'ECONNABORTED' ||
      error.code === 'ERR_NETWORK' ||
      error.code === 'ECONNREFUSED'
    ) {
      console.warn(`[Backend Offline] Port 8080 unreachable. Handling via in-browser data store for ${originalRequest.url}`);
      try {
        const mockResult = handleMockRoute(
          originalRequest.method || 'get',
          originalRequest.url || '',
          originalRequest.data
        );
        return Promise.resolve(mockResult);
      } catch (mockErr) {
        return Promise.reject(mockErr);
      }
    }

    return Promise.reject(error.response?.data || error);
  }
);

export default axiosClient;
