import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Pill,
  Clock,
  Activity,
  UserCheck,
  Calendar,
  FileText,
  Bell,
  Settings,
  ShieldAlert,
  Users,
  Layers,
  FileCheck2,
  CalendarCheck
} from 'lucide-react';

export default function Sidebar() {
  const { isPatient, isDoctor, isAdmin } = useAuth();

  const patientLinks = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Today\'s Reminders', path: '/reminders', icon: Clock },
    { name: 'My Medicines', path: '/medicines', icon: Pill },
    { name: 'Med Schedule', path: '/schedules', icon: CalendarCheck },
    { name: 'Health Tracker', path: '/health-tracker', icon: Activity },
    { name: 'Doctors & Care', path: '/doctors', icon: UserCheck },
    { name: 'Appointments', path: '/appointments', icon: Calendar },
    { name: 'Clinical Reports', path: '/reports', icon: FileText },
    { name: 'Notifications', path: '/notifications', icon: Bell },
    { name: 'Settings & Profile', path: '/profile', icon: Settings },
  ];

  const doctorLinks = [
    { name: 'Doctor Overview', path: '/doctor/dashboard', icon: LayoutDashboard },
    { name: 'Appointments', path: '/doctor/appointments', icon: Calendar },
    { name: 'Patient Directory', path: '/doctor/patients', icon: Users },
    { name: 'Prescriptions', path: '/doctor/prescriptions', icon: FileCheck2 },
    { name: 'Profile & Hours', path: '/profile', icon: Settings },
  ];

  const adminLinks = [
    { name: 'Admin Console', path: '/admin/dashboard', icon: ShieldAlert },
    { name: 'User Management', path: '/admin/users', icon: Users },
    { name: 'Medicine Categories', path: '/admin/categories', icon: Layers },
    { name: 'Audit Logs', path: '/admin/audit', icon: FileText },
    { name: 'System Settings', path: '/profile', icon: Settings },
  ];

  const links = isAdmin ? adminLinks : isDoctor ? doctorLinks : patientLinks;

  return (
    <aside className="w-64 bg-white border-r border-slate-200 min-h-[calc(100vh-4rem)] flex flex-col justify-between py-6 px-4">
      <div className="space-y-1.5">
        <div className="px-3 mb-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">
          {isAdmin ? 'System Administration' : isDoctor ? 'Physician Portal' : 'Patient Management'}
        </div>
        {links.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-brand-50 text-brand-700 shadow-sm font-semibold'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                }`
              }
            >
              <Icon className="w-4 h-4 mr-3 shrink-0" />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </div>

      <div className="p-4 bg-gradient-to-br from-brand-50 to-sky-50 rounded-2xl border border-brand-100">
        <div className="flex items-center space-x-2 text-brand-800 font-semibold text-xs mb-1">
          <Activity className="w-4 h-4 text-brand-600" />
          <span>Health Tip of the Day</span>
        </div>
        <p className="text-xs text-slate-600 leading-relaxed">
          Stay hydrated! Drinking water 30 minutes before medicine improves metabolic absorption.
        </p>
      </div>
    </aside>
  );
}
