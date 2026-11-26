import React, { useState, useEffect } from 'react';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { playReminderBeep } from '../../utils/audioAlert';
import { Clock, CheckCircle, AlertTriangle, RefreshCw, Volume2, ShieldAlert } from 'lucide-react';

export default function RemindersPage() {
  const [summary, setSummary] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [tab, setTab] = useState('today'); // 'today' | 'history'

  const fetchReminders = async () => {
    try {
      const sumRes = await axiosClient.get('/api/v1/doses/today/summary');
      if (sumRes?.success) setSummary(sumRes.data);

      const histRes = await axiosClient.get('/api/v1/doses/history?size=20');
      if (histRes?.success) setHistory(histRes.data?.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReminders();
  }, []);

  const handleTake = async (id) => {
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${id}/take`);
      playReminderBeep();
      await fetchReminders();
    } catch (err) {
      alert(err.message || 'Error recording dose');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSkip = async (id) => {
    const reason = prompt('Reason for skipping dose (optional):', 'Side effects / Fasting');
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${id}/skip`, { reason });
      await fetchReminders();
    } catch (err) {
      alert(err.message || 'Error skipping dose');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSnooze = async (id) => {
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${id}/snooze`, { snoozeMinutes: 15 });
      playReminderBeep();
      await fetchReminders();
    } catch (err) {
      alert(err.message || 'Error snoozing dose');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading reminder alarms..." />;

  const doses = summary?.todayDoses || [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Medication Reminders</h1>
          <p className="text-xs text-slate-500">Acknowledge today's prescribed dosages or review adherence history</p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={playReminderBeep}
            className="px-3.5 py-2 rounded-xl text-xs font-semibold bg-brand-50 hover:bg-brand-100 text-brand-700 border border-brand-200 transition-all flex items-center space-x-1.5"
            title="Test medicine reminder alarm chime"
          >
            <Volume2 className="w-4 h-4 text-brand-600 animate-pulse" />
            <span>Test Alarm Sound</span>
          </button>
          <button
            onClick={() => setTab('today')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
              tab === 'today' ? 'bg-brand-600 text-white shadow-sm' : 'bg-white text-slate-600 border border-slate-200'
            }`}
          >
            Today's Schedule ({doses.length})
          </button>
          <button
            onClick={() => setTab('history')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
              tab === 'history' ? 'bg-brand-600 text-white shadow-sm' : 'bg-white text-slate-600 border border-slate-200'
            }`}
          >
            Adherence History
          </button>
        </div>
      </div>

      {/* Summary Scorecard */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-xs text-slate-400 font-semibold block">Scheduled Today</span>
          <div className="text-xl font-bold text-slate-900 mt-1">{summary?.totalScheduledToday || 0} doses</div>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-xs text-emerald-600 font-semibold block">Taken</span>
          <div className="text-xl font-bold text-emerald-700 mt-1">{summary?.takenToday || 0}</div>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-xs text-rose-600 font-semibold block">Missed / Overdue</span>
          <div className="text-xl font-bold text-rose-700 mt-1">{summary?.missedToday || 0}</div>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-xs text-brand-600 font-semibold block">Today's Adherence</span>
          <div className="text-xl font-bold text-brand-700 mt-1">{summary?.adherenceRateToday || 0}%</div>
        </div>
      </div>

      {tab === 'today' ? (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-4">
          <h2 className="text-base font-bold text-slate-900">Today's Dosage Queue</h2>

          {doses.length === 0 ? (
            <div className="text-center py-12 text-slate-400 text-sm">
              No doses scheduled for today. Add medications with reminder schedules to populate.
            </div>
          ) : (
            <div className="space-y-4">
              {doses.map((dose) => (
                <div
                  key={dose.id}
                  className={`p-5 rounded-2xl border transition-all flex flex-col md:flex-row justify-between items-start md:items-center gap-4 ${
                    dose.status === 'TAKEN'
                      ? 'bg-emerald-50/50 border-emerald-200'
                      : dose.status === 'MISSED'
                      ? 'bg-rose-50/50 border-rose-200'
                      : 'bg-white border-slate-200 hover:border-brand-200 shadow-sm'
                  }`}
                >
                  <div className="flex items-start space-x-4">
                    <div className={`w-12 h-12 rounded-xl flex items-center justify-center font-bold text-sm shrink-0 ${
                      dose.status === 'TAKEN'
                        ? 'bg-emerald-100 text-emerald-700'
                        : dose.status === 'MISSED'
                        ? 'bg-rose-100 text-rose-700'
                        : 'bg-brand-100 text-brand-700'
                    }`}>
                      <Clock className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="flex items-center space-x-2">
                        <h3 className="font-bold text-base text-slate-900">{dose.medicineName}</h3>
                        <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                          dose.status === 'TAKEN'
                            ? 'bg-emerald-100 text-emerald-800'
                            : dose.status === 'MISSED'
                            ? 'bg-rose-100 text-rose-800'
                            : dose.status === 'SKIPPED'
                            ? 'bg-slate-100 text-slate-700'
                            : 'bg-amber-100 text-amber-800'
                        }`}>
                          {dose.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-500 mt-1">
                        Scheduled for: <strong className="text-slate-700">{dose.scheduledTime?.substring(11, 16)}</strong>
                        {dose.actualTime && <span> | Taken at: {dose.actualTime.substring(11, 16)}</span>}
                        {dose.skipReason && <span> | Reason: {dose.skipReason}</span>}
                        {dose.snoozeCount > 0 && <span> | Snoozed ({dose.snoozeCount}x)</span>}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2 w-full md:w-auto">
                    {dose.status !== 'TAKEN' && (
                      <button
                        onClick={() => handleTake(dose.id)}
                        disabled={actionLoading}
                        className="flex-1 md:flex-initial px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-sm flex items-center justify-center space-x-1.5 transition-all"
                      >
                        <CheckCircle className="w-4 h-4" />
                        <span>TAKE MEDICINE</span>
                      </button>
                    )}

                    {dose.status === 'SCHEDULED' && (
                      <>
                        <button
                          onClick={() => handleSnooze(dose.id)}
                          disabled={actionLoading}
                          className="px-3 py-2 bg-amber-50 hover:bg-amber-100 text-amber-700 rounded-xl text-xs font-semibold transition-colors"
                        >
                          Snooze (15m)
                        </button>
                        <button
                          onClick={() => handleSkip(dose.id)}
                          disabled={actionLoading}
                          className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-xs font-semibold transition-colors"
                        >
                          Skip
                        </button>
                      </>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
          <h2 className="text-base font-bold text-slate-900 mb-4">Historical Dose Log Entries</h2>
          <div className="divide-y divide-slate-100">
            {history.map((h) => (
              <div key={h.id} className="py-3 flex justify-between items-center text-xs">
                <div>
                  <strong className="text-slate-800 text-sm block">{h.medicineName}</strong>
                  <span className="text-slate-400">
                    {h.scheduledTime?.substring(0, 10)} at {h.scheduledTime?.substring(11, 16)}
                  </span>
                </div>
                <div>
                  <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                    h.status === 'TAKEN' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-600'
                  }`}>
                    {h.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
