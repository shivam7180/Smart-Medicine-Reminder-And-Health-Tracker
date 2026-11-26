import React, { createContext, useContext, useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('medicare_access_token');
    const savedUser = localStorage.getItem('medicare_user');

    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.clear();
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await axiosClient.post('/api/v1/auth/login', { email, password });
      if (response.success && response.data) {
        const authData = response.data;
        setToken(authData.accessToken);
        setUser({
          id: authData.userId,
          email: authData.email,
          fullName: authData.fullName,
          roles: authData.roles,
        });

        localStorage.setItem('medicare_access_token', authData.accessToken);
        localStorage.setItem('medicare_refresh_token', authData.refreshToken);
        localStorage.setItem('medicare_user', JSON.stringify({
          id: authData.userId,
          email: authData.email,
          fullName: authData.fullName,
          roles: authData.roles,
        }));

        return { success: true };
      }
      return { success: false, message: response.message || 'Login failed' };
    } catch (error) {
      return { success: false, message: error.message || 'Invalid credentials' };
    }
  };

  const register = async (userData) => {
    try {
      const response = await axiosClient.post('/api/v1/auth/register', userData);
      if (response.success && response.data) {
        const authData = response.data;
        setToken(authData.accessToken);
        setUser({
          id: authData.userId,
          email: authData.email,
          fullName: authData.fullName,
          roles: authData.roles,
        });

        localStorage.setItem('medicare_access_token', authData.accessToken);
        localStorage.setItem('medicare_refresh_token', authData.refreshToken);
        localStorage.setItem('medicare_user', JSON.stringify({
          id: authData.userId,
          email: authData.email,
          fullName: authData.fullName,
          roles: authData.roles,
        }));

        return { success: true };
      }
      return { success: false, message: response.message || 'Registration failed' };
    } catch (error) {
      return { success: false, message: error.message || 'Registration failed' };
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('medicare_access_token');
    localStorage.removeItem('medicare_refresh_token');
    localStorage.removeItem('medicare_user');
  };

  const hasRole = (roleName) => {
    if (!user || !user.roles) return false;
    const target = roleName.toUpperCase();
    return user.roles.some((r) => r === target || r === `ROLE_${target}`);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated: !!token,
        isPatient: hasRole('PATIENT'),
        isDoctor: hasRole('DOCTOR'),
        isAdmin: hasRole('ADMIN'),
        login,
        register,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
