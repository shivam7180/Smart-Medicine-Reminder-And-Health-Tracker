import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import DashboardLayout from './components/layout/DashboardLayout';

// Public Pages
import LandingPage from './pages/public/LandingPage';
import AboutPage from './pages/public/AboutPage';
import FeaturesPage from './pages/public/FeaturesPage';
import ContactPage from './pages/public/ContactPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/auth/ResetPasswordPage';

// Patient Pages
import PatientDashboard from './pages/patient/PatientDashboard';
import MedicinesPage from './pages/patient/MedicinesPage';
import AddMedicinePage from './pages/patient/AddMedicinePage';
import MedicineSchedulePage from './pages/patient/MedicineSchedulePage';
import RemindersPage from './pages/patient/RemindersPage';
import HealthTrackerPage from './pages/patient/HealthTrackerPage';
import DoctorsPage from './pages/patient/DoctorsPage';
import AppointmentsPage from './pages/patient/AppointmentsPage';
import ReportsPage from './pages/patient/ReportsPage';
import NotificationsPage from './pages/patient/NotificationsPage';
import ProfileSettingsPage from './pages/patient/ProfileSettingsPage';

// Doctor & Admin
import DoctorDashboard from './pages/doctor/DoctorDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return null;
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Pages */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/features" element={<FeaturesPage />} />
          <Route path="/contact" element={<ContactPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          {/* Protected Patient & Healthcare Workspace */}
          <Route
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<PatientDashboard />} />
            <Route path="/reminders" element={<RemindersPage />} />
            <Route path="/medicines" element={<MedicinesPage />} />
            <Route path="/medicines/new" element={<AddMedicinePage />} />
            <Route path="/schedules" element={<MedicineSchedulePage />} />
            <Route path="/health-tracker" element={<HealthTrackerPage />} />
            <Route path="/doctors" element={<DoctorsPage />} />
            <Route path="/appointments" element={<AppointmentsPage />} />
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/profile" element={<ProfileSettingsPage />} />

            {/* Doctor Routes */}
            <Route path="/doctor/dashboard" element={<DoctorDashboard />} />
            <Route path="/doctor/appointments" element={<DoctorDashboard />} />
            <Route path="/doctor/patients" element={<DoctorDashboard />} />
            <Route path="/doctor/prescriptions" element={<MedicinesPage />} />

            {/* Admin Routes */}
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<AdminDashboard />} />
            <Route path="/admin/categories" element={<AdminDashboard />} />
            <Route path="/admin/audit" element={<AdminDashboard />} />
          </Route>

          {/* Catch-all redirect */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}
