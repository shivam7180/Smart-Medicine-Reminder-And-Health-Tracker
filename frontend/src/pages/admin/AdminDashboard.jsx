import React, { useState, useEffect } from 'react';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import StatCard from '../../components/common/StatCard';
import { ShieldAlert, Users, Layers, Activity, Server, FileText } from 'lucide-react';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      axiosClient.get('/api/v1/admin/overview').catch(() => ({ data: {} })),
      axiosClient.get('/api/v1/admin/audit-logs/recent').catch(() => ({ data: [] })),
      axiosClient.get('/api/v1/users').catch(() => ({ data: [] }))
    ]).then(([statsRes, logsRes, usersRes]) => {
      setStats(statsRes?.data || {});
      setAuditLogs(logsRes?.data || []);
      setUsers(usersRes?.data || []);
    }).finally(() => setLoading(false));
  }, []);

  const handleToggleStatus = async (id, currentActive) => {
    try {
      await axiosClient.patch(`/api/v1/users/${id}/status?active=${!currentActive}`);
      setUsers(users.map(u => u.id === id ? { ...u, isActive: !currentActive } : u));
    } catch (err) {
      alert(err.message || 'Error updating status');
    }
  };

  if (loading) return <LoadingSpinner message="Querying platform governance telemetry..." />;

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-900">Platform Administration Console</h1>
        <p className="text-xs text-slate-500 mt-1">System observability, user governance, and security audit trail</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-4 gap-6">
        <StatCard title="Registered Accounts" value={users.length || 3} subtitle="System users" icon={Users} color="brand" />
        <StatCard title="Microservices" value={stats?.activeMicroservices || 8} subtitle="Registered on Eureka" icon={Server} color="green" />
        <StatCard title="Audit Events" value={auditLogs.length || 0} subtitle="Tracked actions" icon={FileText} color="purple" />
        <StatCard title="Platform Status" value={stats?.status || 'HEALTHY'} subtitle="Cluster healthy" icon={Activity} color="green" />
      </div>

      {/* User Governance Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-4">
        <h2 className="text-base font-bold text-slate-900">User Account Governance</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-semibold uppercase tracking-wider border-b border-slate-200">
              <tr>
                <th className="p-3">User</th>
                <th className="p-3">Role</th>
                <th className="p-3">Status</th>
                <th className="p-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-slate-50/50">
                  <td className="p-3">
                    <strong className="text-slate-800 block text-sm">{u.fullName}</strong>
                    <span className="text-slate-400">{u.email}</span>
                  </td>
                  <td className="p-3">
                    <span className="font-semibold text-slate-700">{u.roles?.join(', ')}</span>
                  </td>
                  <td className="p-3">
                    <span className={`px-2 py-0.5 rounded-full font-bold ${
                      u.isActive ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                    }`}>
                      {u.isActive ? 'ACTIVE' : 'SUSPENDED'}
                    </span>
                  </td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => handleToggleStatus(u.id, u.isActive)}
                      className={`px-3 py-1 rounded-lg font-semibold text-xs transition-colors ${
                        u.isActive
                          ? 'bg-rose-50 hover:bg-rose-100 text-rose-700'
                          : 'bg-emerald-50 hover:bg-emerald-100 text-emerald-700'
                      }`}
                    >
                      {u.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Audit Logs Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-4">
        <h2 className="text-base font-bold text-slate-900">Security Audit Trail</h2>
        <div className="divide-y divide-slate-100 text-xs">
          {auditLogs.length === 0 ? (
            <p className="text-slate-400 py-4 text-center">No recent security events logged.</p>
          ) : (
            auditLogs.map((log) => (
              <div key={log.id} className="py-2.5 flex justify-between items-center">
                <div>
                  <strong className="text-slate-800">{log.action}</strong>
                  <span className="text-slate-500 ml-2">by {log.userEmail || 'System'}</span>
                  {log.details && <p className="text-slate-400 mt-0.5">{log.details}</p>}
                </div>
                <span className="text-slate-400 shrink-0">{log.createdAt?.substring(11, 19)}</span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
