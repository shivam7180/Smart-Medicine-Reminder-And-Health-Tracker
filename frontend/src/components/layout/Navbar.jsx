import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import { Bell, HeartPulse, User, LogOut, ChevronDown, CheckCircle, ShieldAlert } from 'lucide-react';

export default function Navbar() {
  const { user, logout, isAuthenticated, isPatient, isDoctor, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      axiosClient
        .get('/api/v1/notifications/unread/count')
        .then((res) => {
          if (res?.success) setUnreadCount(res.data || 0);
        })
        .catch(() => {});
    }
  }, [isAuthenticated]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2.5 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-brand-400 flex items-center justify-center text-white shadow-md shadow-brand-500/20 group-hover:scale-105 transition-transform">
              <HeartPulse className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight text-slate-900 flex items-center">
                Medi<span className="text-brand-600">Care</span>
              </span>
              <div className="flex items-center space-x-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                <span className="text-[10px] text-slate-400 font-medium tracking-wide uppercase">
                  Connected & Ready
                </span>
              </div>
            </div>
          </Link>

          {/* Navigation Links (Public) */}
          {!isAuthenticated ? (
            <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-slate-600">
              <Link to="/" className="hover:text-brand-600 transition-colors">Home</Link>
              <Link to="/features" className="hover:text-brand-600 transition-colors">Features</Link>
              <Link to="/about" className="hover:text-brand-600 transition-colors">About Us</Link>
              <Link to="/contact" className="hover:text-brand-600 transition-colors">Contact</Link>
            </nav>
          ) : null}

          {/* User Controls */}
          <div className="flex items-center space-x-4">
            {!isAuthenticated ? (
              <div className="flex items-center space-x-3">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-semibold text-slate-700 hover:text-brand-600 transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg shadow-sm transition-all shadow-brand-500/20"
                >
                  Get Started
                </Link>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                {/* Notification Bell */}
                <Link
                  to="/notifications"
                  className="relative p-2 text-slate-500 hover:text-brand-600 hover:bg-slate-100 rounded-full transition-colors"
                  title="Notifications"
                >
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 w-4 h-4 bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </Link>

                {/* User Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setDropdownOpen(!dropdownOpen)}
                    className="flex items-center space-x-2.5 p-1.5 rounded-lg hover:bg-slate-100 transition-colors text-left"
                  >
                    <div className="w-8 h-8 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center font-bold text-sm">
                      {user?.fullName?.charAt(0) || 'U'}
                    </div>
                    <div className="hidden sm:block text-xs">
                      <div className="font-semibold text-slate-800">{user?.fullName || 'User'}</div>
                      <div className="text-slate-400 capitalize">
                        {isAdmin ? 'Administrator' : isDoctor ? 'Physician' : 'Patient'}
                      </div>
                    </div>
                    <ChevronDown className="w-4 h-4 text-slate-400" />
                  </button>

                  {dropdownOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-lg border border-slate-100 py-1.5 text-sm z-50">
                      <div className="px-4 py-2 border-b border-slate-100 text-xs text-slate-500">
                        Signed in as <strong className="text-slate-700">{user?.email}</strong>
                      </div>
                      <Link
                        to="/profile"
                        onClick={() => setDropdownOpen(false)}
                        className="flex items-center px-4 py-2 text-slate-700 hover:bg-brand-50 hover:text-brand-700"
                      >
                        <User className="w-4 h-4 mr-2.5" /> My Profile
                      </Link>
                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center px-4 py-2 text-rose-600 hover:bg-rose-50 text-left"
                      >
                        <LogOut className="w-4 h-4 mr-2.5" /> Sign Out
                      </button>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
