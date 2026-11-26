import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import { playReminderBeep, playUrgentBeep } from '../../utils/audioAlert';
import { Bell, Volume2, CheckCircle, Clock, X } from 'lucide-react';

export default function ReminderAlarmNotifier() {
  const { user, isPatient } = useAuth();
  const [activeDose, setActiveDose] = useState(null);
  const [alarmDismissed, setAlarmDismissed] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const lastBeepedDoseId = useRef(null);

  const checkDueReminders = async () => {
    if (!user || !isPatient) return;
    try {
      const res = await axiosClient.get('/api/v1/doses/today/summary');
      if (res?.success && res.data?.todayDoses) {
        const doses = res.data.todayDoses;
        const now = new Date();
        const currentHours = now.getHours();
        const currentMins = now.getMinutes();
        const currentMinutesOfDay = currentHours * 60 + currentMins;

        // Find dose that is SCHEDULED or SNOOZED and due right now (or within today's window)
        const dueDose = doses.find((d) => {
          if (d.status !== 'SCHEDULED' && d.status !== 'SNOOZED') return false;
          if (!d.scheduledTime) return false;
          const timePart = d.scheduledTime.substring(11, 16); // "08:00"
          const [h, m] = timePart.split(':').map(Number);
          const doseMinutesOfDay = h * 60 + m;

          // Trigger if dose is within 15 mins past schedule or next upcoming
          return Math.abs(currentMinutesOfDay - doseMinutesOfDay) <= 30 || d.status === 'SNOOZED';
        });

        if (dueDose) {
          setActiveDose(dueDose);
          // Play beep sound automatically once per due dose
          if (lastBeepedDoseId.current !== dueDose.id) {
            lastBeepedDoseId.current = dueDose.id;
            playReminderBeep();
          }
        } else {
          setActiveDose(null);
        }
      }
    } catch (e) {
      // Quietly ignore network failures in background ticker
    }
  };

  useEffect(() => {
    if (!isPatient) return;
    checkDueReminders();
    // Check every 30 seconds for upcoming dose times
    const interval = setInterval(checkDueReminders, 30000);
    return () => clearInterval(interval);
  }, [isPatient, user]);

  const handleTake = async () => {
    if (!activeDose) return;
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${activeDose.id}/take`);
      playReminderBeep();
      setActiveDose(null);
      setAlarmDismissed(true);
      window.location.reload();
    } catch (e) {
      alert(e.message || 'Error recording dose');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSnooze = async () => {
    if (!activeDose) return;
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${activeDose.id}/snooze`, { snoozeMinutes: 15 });
      playReminderBeep();
      setActiveDose(null);
      setAlarmDismissed(true);
      window.location.reload();
    } catch (e) {
      alert(e.message || 'Error snoozing dose');
    } finally {
      setActionLoading(false);
    }
  };

  if (!activeDose || alarmDismissed) return null;

  return (
    <div className="bg-gradient-to-r from-amber-500 via-amber-600 to-rose-600 text-white px-4 py-3 shadow-lg flex flex-col sm:flex-row items-center justify-between gap-3 animate-bounce-subtle z-50">
      <div className="flex items-center space-x-3">
        <div className="w-9 h-9 rounded-xl bg-white/20 backdrop-blur-md flex items-center justify-center shrink-0">
          <Bell className="w-5 h-5 text-white animate-pulse" />
        </div>
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-[11px] font-bold uppercase tracking-wider bg-black/20 px-2 py-0.5 rounded-full">
              MEDICINE REMINDER ALARM
            </span>
            <span className="text-xs text-amber-100 font-medium">
              Scheduled {activeDose.scheduledTime?.substring(11, 16)}
            </span>
          </div>
          <h4 className="text-sm font-extrabold text-white mt-0.5">
            Time to take: <span className="underline decoration-white/40">{activeDose.medicineName}</span>
          </h4>
        </div>
      </div>

      <div className="flex items-center space-x-2">
        <button
          onClick={playReminderBeep}
          title="Play reminder chime again"
          className="px-3 py-1.5 bg-white/20 hover:bg-white/30 text-white rounded-xl text-xs font-semibold backdrop-blur-md flex items-center space-x-1.5 transition-colors"
        >
          <Volume2 className="w-3.5 h-3.5" />
          <span>Replay Beep</span>
        </button>

        <button
          onClick={handleTake}
          disabled={actionLoading}
          className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-sm transition-all flex items-center space-x-1"
        >
          <CheckCircle className="w-3.5 h-3.5" />
          <span>TAKE NOW</span>
        </button>

        <button
          onClick={handleSnooze}
          disabled={actionLoading}
          className="px-3 py-1.5 bg-white/20 hover:bg-white/30 text-white rounded-xl text-xs font-semibold backdrop-blur-md transition-colors"
        >
          Snooze 15m
        </button>

        <button
          onClick={() => setAlarmDismissed(true)}
          className="p-1.5 hover:bg-white/20 rounded-lg text-white/80 hover:text-white transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
