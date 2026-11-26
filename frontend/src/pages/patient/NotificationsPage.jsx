import React, { useState, useEffect } from 'react';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Bell, CheckCheck, Trash2, Clock, AlertTriangle } from 'lucide-react';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = async () => {
    try {
      const res = await axiosClient.get('/api/v1/notifications?size=30');
      if (res?.success) setNotifications(res.data?.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkAll = async () => {
    await axiosClient.patch('/api/v1/notifications/read-all');
    fetchNotifications();
  };

  const handleMarkOne = async (id) => {
    await axiosClient.patch(`/api/v1/notifications/${id}/read`);
    fetchNotifications();
  };

  const handleDelete = async (id) => {
    await axiosClient.delete(`/api/v1/notifications/${id}`);
    fetchNotifications();
  };

  if (loading) return <LoadingSpinner message="Loading notifications..." />;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Notifications Center</h1>
          <p className="text-xs text-slate-500">Medicine dosage alarms, low inventory warnings, and clinic updates</p>
        </div>
        {notifications.some(n => !n.isRead) && (
          <button
            onClick={handleMarkAll}
            className="px-3.5 py-2 bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 rounded-xl text-xs font-semibold flex items-center space-x-1.5 shadow-sm"
          >
            <CheckCheck className="w-4 h-4 text-brand-600" />
            <span>Mark All Read</span>
          </button>
        )}
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm divide-y divide-slate-100 overflow-hidden">
        {notifications.length === 0 ? (
          <div className="text-center py-12 text-slate-400 text-sm">
            No notifications in your feed.
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              className={`p-4 sm:p-5 flex justify-between items-start gap-4 transition-colors ${
                !n.isRead ? 'bg-brand-50/40' : 'bg-white'
              }`}
            >
              <div className="flex items-start space-x-3">
                <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${
                  n.notificationType === 'MISSED_DOSE'
                    ? 'bg-rose-100 text-rose-700'
                    : n.notificationType === 'LOW_STOCK'
                    ? 'bg-amber-100 text-amber-700'
                    : 'bg-brand-100 text-brand-700'
                }`}>
                  <Bell className="w-4 h-4" />
                </div>
                <div>
                  <h4 className={`text-sm font-semibold ${!n.isRead ? 'text-slate-900' : 'text-slate-700'}`}>
                    {n.title}
                  </h4>
                  <p className="text-xs text-slate-500 mt-0.5 leading-relaxed">{n.message}</p>
                  <span className="text-[10px] text-slate-400 mt-1 block">
                    {n.createdAt?.substring(0, 10)} at {n.createdAt?.substring(11, 16)}
                  </span>
                </div>
              </div>

              <div className="flex items-center space-x-2 shrink-0">
                {!n.isRead && (
                  <button
                    onClick={() => handleMarkOne(n.id)}
                    className="p-1 text-slate-400 hover:text-brand-600 text-xs font-semibold"
                    title="Mark as read"
                  >
                    Mark Read
                  </button>
                )}
                <button
                  onClick={() => handleDelete(n.id)}
                  className="p-1 text-slate-400 hover:text-rose-600"
                  title="Delete"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
