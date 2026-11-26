import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import StatCard from '../../components/common/StatCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { playReminderBeep } from '../../utils/audioAlert';
import {
  Pill,
  Clock,
  CheckCircle,
  AlertTriangle,
  Heart,
  Activity,
  Calendar,
  ChevronRight,
  TrendingUp,
  Droplet,
  Zap,
  ArrowUpRight,
  ShieldAlert,
  Volume2
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';

export default function PatientDashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchDashboardData = async () => {
    try {
      const res = await axiosClient.get('/api/v1/analytics/dashboard/patient');
      if (res?.success) {
        setData(res.data);
      }
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleTakeDose = async (doseId) => {
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${doseId}/take`);
      playReminderBeep();
      await fetchDashboardData();
    } catch (err) {
      alert(err.message || 'Error recording dose');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSkipDose = async (doseId) => {
    const reason = prompt('Optional reason for skipping dose:', 'Doctor advised / feeling unwell');
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${doseId}/skip`, { reason });
      await fetchDashboardData();
    } catch (err) {
      alert(err.message || 'Error skipping dose');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSnoozeDose = async (doseId) => {
    setActionLoading(true);
    try {
      await axiosClient.post(`/api/v1/doses/${doseId}/snooze`, { snoozeMinutes: 15 });
      playReminderBeep();
      await fetchDashboardData();
    } catch (err) {
      alert(err.message || 'Error snoozing reminder');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Aggregating clinical metrics & today's dosage..." />;
  }

  const today = data?.todayReminders || {};
  const adherence = data?.adherenceStats || {};
  const vitals = data?.latestVitals || {};
  const nextReminder = today?.nextUpcomingReminder;

  // Chart data formatters
  const bpChartData = (data?.bloodPressureTrends || []).map((item) => ({
    time: item.readingTime?.substring(5, 10) || '',
    systolic: item.valuePrimary || 120,
    diastolic: item.valueSecondary || 80,
  }));

  const sugarChartData = (data?.bloodSugarTrends || []).map((item) => ({
    time: item.readingTime?.substring(5, 10) || '',
    glucose: item.valuePrimary || 100,
  }));

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-brand-700 via-brand-600 to-sky-600 rounded-3xl p-6 sm:p-8 text-white shadow-lg shadow-brand-500/15 relative overflow-hidden">
        <div className="absolute right-0 top-0 bottom-0 w-1/3 opacity-10 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-white via-white to-transparent pointer-events-none"></div>
        <div className="relative z-10 max-w-2xl">
          <span className="px-3 py-1 rounded-full bg-white/20 text-white text-xs font-semibold backdrop-blur-sm uppercase tracking-wide">
            Patient Health OS
          </span>
          <h1 className="mt-3 text-2xl sm:text-3xl font-extrabold tracking-tight">
            Welcome, {user?.fullName || 'Patient'}!
          </h1>
          <p className="mt-2 text-brand-100 text-sm sm:text-base leading-relaxed">
            {data?.activeMedicinesCount === 0
              ? 'Welcome to your real-time health dashboard. Get started by adding your prescribed medicines to enable automated reminders.'
              : `Your current 30-day medication adherence is at ${adherence?.adherencePercentage ?? 100}%. You have ${today?.pendingToday ?? 0} dose${today?.pendingToday === 1 ? '' : 's'} remaining today.`}
          </p>
        </div>
      </div>

      {/* Onboarding Banner for Clean New Users with 0 Medicines */}
      {data?.activeMedicinesCount === 0 && (
        <div className="bg-gradient-to-r from-brand-50 via-sky-50 to-indigo-50 border-2 border-dashed border-brand-300 rounded-3xl p-6 sm:p-8 flex flex-col md:flex-row items-start md:items-center justify-between gap-6 shadow-sm">
          <div className="flex items-start space-x-4">
            <div className="w-14 h-14 rounded-2xl bg-brand-600 text-white flex items-center justify-center shrink-0 shadow-md">
              <Pill className="w-7 h-7" />
            </div>
            <div>
              <span className="px-2.5 py-0.5 rounded-full bg-brand-100 text-brand-800 text-xs font-bold uppercase tracking-wider">
                Getting Started
              </span>
              <h3 className="text-xl font-bold text-slate-900 mt-1">Add Your Prescribed Medications</h3>
              <p className="text-sm text-slate-600 mt-1 max-w-xl">
                You currently have 0 medications registered. Add your prescriptions or supplements, choose your daily reminder times (e.g. 08:00 AM, 08:00 PM), and your dose schedule will automatically activate with Take, Skip, and Snooze actions.
              </p>
            </div>
          </div>
          <Link
            to="/medicines/new"
            className="px-6 py-3.5 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-2xl text-sm shadow-md shadow-brand-500/20 transition-all shrink-0 flex items-center space-x-2"
          >
            <span>+ Add Your First Medicine</span>
          </Link>
        </div>
      )}

      {/* Top Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <StatCard
          title="Active Medicines"
          value={data?.activeMedicinesCount || 0}
          subtitle="Currently prescribed"
          icon={Pill}
          color="brand"
        />
        <StatCard
          title="Today's Doses"
          value={`${today?.takenToday || 0} / ${today?.totalScheduledToday || 0}`}
          subtitle="Doses taken today"
          icon={CheckCircle}
          color="green"
        />
        <StatCard
          title="Missed Doses"
          value={today?.missedToday || 0}
          subtitle="Overdue doses"
          icon={AlertTriangle}
          color={today?.missedToday > 0 ? 'rose' : 'amber'}
        />
        <StatCard
          title="Adherence Rate"
          value={`${data?.activeMedicinesCount === 0 ? '100' : (adherence?.adherencePercentage ?? 100)}%`}
          subtitle="Past 30 days"
          icon={TrendingUp}
          color="purple"
        />
      </div>

      {/* Next Upcoming Reminder Callout */}
      {nextReminder ? (
        <div className="bg-white rounded-2xl border-2 border-brand-200 p-6 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div className="flex items-start space-x-4">
            <div className="w-12 h-12 rounded-xl bg-brand-100 text-brand-700 flex items-center justify-center shrink-0">
              <Clock className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-xs font-bold text-brand-600 uppercase tracking-wider">Next Upcoming Dose</span>
                <span className="px-2 py-0.5 rounded-full bg-brand-50 text-brand-700 text-xs font-semibold">
                  Scheduled {nextReminder.scheduledTime?.substring(11, 16)}
                </span>
              </div>
              <h3 className="text-lg font-bold text-slate-900 mt-1">{nextReminder.medicineName}</h3>
              <p className="text-xs text-slate-500">Take according to prescription instructions.</p>
            </div>
          </div>

          <div className="flex items-center space-x-2 w-full md:w-auto">
            <button
              onClick={playReminderBeep}
              title="Play reminder alarm chime"
              className="px-3 py-2 bg-brand-50 hover:bg-brand-100 text-brand-700 font-semibold rounded-xl text-xs transition-colors flex items-center space-x-1"
            >
              <Volume2 className="w-3.5 h-3.5 animate-pulse text-brand-600" />
              <span>Beep</span>
            </button>
            <button
              onClick={() => handleTakeDose(nextReminder.id)}
              disabled={actionLoading}
              className="flex-1 md:flex-initial px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-xl text-xs shadow-sm transition-all flex items-center justify-center space-x-1.5"
            >
              <CheckCircle className="w-4 h-4" />
              <span>TAKE NOW</span>
            </button>
            <button
              onClick={() => handleSnoozeDose(nextReminder.id)}
              disabled={actionLoading}
              className="px-3 py-2 bg-amber-50 hover:bg-amber-100 text-amber-700 font-semibold rounded-xl text-xs transition-colors"
            >
              Snooze (+15m)
            </button>
            <button
              onClick={() => handleSkipDose(nextReminder.id)}
              disabled={actionLoading}
              className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 font-semibold rounded-xl text-xs transition-colors"
            >
              Skip
            </button>
          </div>
        </div>
      ) : data?.activeMedicinesCount === 0 ? (
        <div className="bg-slate-50 border border-slate-200 rounded-2xl p-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
          <div className="flex items-center space-x-3 text-slate-600 text-xs font-medium">
            <Clock className="w-4 h-4 text-slate-400 shrink-0" />
            <span>No upcoming reminders in queue. Add your first medicine to activate automatic dose tracking.</span>
          </div>
          <Link to="/medicines/new" className="text-xs font-bold text-brand-600 hover:text-brand-700">
            + Add Medicine
          </Link>
        </div>
      ) : (
        <div className="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 flex items-center space-x-3 text-emerald-800 text-xs font-medium">
          <CheckCircle className="w-5 h-5 text-emerald-600 shrink-0" />
          <span>All scheduled medication doses for today have been acknowledged! Excellent adherence.</span>
        </div>
      )}

      {/* Vitals Snapshot Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center space-x-2 text-rose-600 text-xs font-semibold mb-1">
              <Heart className="w-4 h-4" />
              <span>Blood Pressure</span>
            </div>
            <div className="text-xl font-bold text-slate-900">
              {vitals.bloodPressureSystolic ? (
                <>
                  {vitals.bloodPressureSystolic}/{vitals.bloodPressureDiastolic}
                  <span className="text-xs text-slate-400 font-normal ml-1">mmHg</span>
                </>
              ) : (
                <span className="text-slate-400 font-medium text-base">-- / --</span>
              )}
            </div>
          </div>
          <Link to="/health-tracker" className="text-[11px] font-semibold text-brand-600 hover:text-brand-700 mt-2 block">
            {vitals.bloodPressureSystolic ? 'Update log' : '+ Record BP'}
          </Link>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center space-x-2 text-brand-600 text-xs font-semibold mb-1">
              <Droplet className="w-4 h-4" />
              <span>Blood Sugar</span>
            </div>
            <div className="text-xl font-bold text-slate-900">
              {vitals.bloodSugar ? (
                <>
                  {vitals.bloodSugar}
                  <span className="text-xs text-slate-400 font-normal ml-1">mg/dL</span>
                </>
              ) : (
                <span className="text-slate-400 font-medium text-base">--</span>
              )}
            </div>
          </div>
          <Link to="/health-tracker" className="text-[11px] font-semibold text-brand-600 hover:text-brand-700 mt-2 block">
            {vitals.bloodSugar ? 'Update log' : '+ Record Sugar'}
          </Link>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center space-x-2 text-purple-600 text-xs font-semibold mb-1">
              <Activity className="w-4 h-4" />
              <span>Heart Rate</span>
            </div>
            <div className="text-xl font-bold text-slate-900">
              {vitals.heartRate ? (
                <>
                  {vitals.heartRate}
                  <span className="text-xs text-slate-400 font-normal ml-1">bpm</span>
                </>
              ) : (
                <span className="text-slate-400 font-medium text-base">--</span>
              )}
            </div>
          </div>
          <Link to="/health-tracker" className="text-[11px] font-semibold text-brand-600 hover:text-brand-700 mt-2 block">
            {vitals.heartRate ? 'Update log' : '+ Record Heart Rate'}
          </Link>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center space-x-2 text-emerald-600 text-xs font-semibold mb-1">
              <Zap className="w-4 h-4" />
              <span>BMI</span>
            </div>
            <div className="text-xl font-bold text-slate-900">
              {vitals.bmi ? (
                <>
                  {vitals.bmi}
                  <span className="text-xs text-slate-400 font-normal ml-1">({vitals.bmiCategory || 'Normal'})</span>
                </>
              ) : (
                <span className="text-slate-400 font-medium text-base">--</span>
              )}
            </div>
          </div>
          <Link to="/health-tracker" className="text-[11px] font-semibold text-brand-600 hover:text-brand-700 mt-2 block">
            {vitals.bmi ? 'Update log' : '+ Log Height/Weight'}
          </Link>
        </div>
      </div>

      {/* Analytics Trend Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Blood Pressure Chart */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="text-base font-bold text-slate-800">Blood Pressure Trends</h3>
              <p className="text-xs text-slate-500">Systolic & Diastolic tracking (mmHg)</p>
            </div>
            <Link to="/health-tracker" className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center">
              View All <ArrowUpRight className="w-3.5 h-3.5 ml-1" />
            </Link>
          </div>
          <div className="h-60 w-full">
            {bpChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={bpChartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="time" stroke="#94a3b8" fontSize={11} />
                  <YAxis domain={[50, 160]} stroke="#94a3b8" fontSize={11} />
                  <Tooltip />
                  <Line type="monotone" dataKey="systolic" stroke="#f43f5e" strokeWidth={2} name="Systolic" />
                  <Line type="monotone" dataKey="diastolic" stroke="#0284c7" strokeWidth={2} name="Diastolic" />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 bg-slate-50/60 rounded-xl border border-dashed border-slate-200">
                <Heart className="w-8 h-8 text-slate-300 mb-2" />
                <p className="text-sm font-semibold text-slate-700">No Blood Pressure Readings Yet</p>
                <p className="text-xs text-slate-400 mt-1 max-w-xs">
                  Log your daily systolic and diastolic values in the Health Tracker to visualize your cardiovascular trends.
                </p>
                <Link to="/health-tracker" className="mt-3 px-3 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 font-semibold rounded-lg text-xs">
                  + Log Blood Pressure
                </Link>
              </div>
            )}
          </div>
        </div>

        {/* Blood Glucose Chart */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="text-base font-bold text-slate-800">Blood Sugar Trends</h3>
              <p className="text-xs text-slate-500">Fasting Glucose level (mg/dL)</p>
            </div>
            <Link to="/health-tracker" className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center">
              View All <ArrowUpRight className="w-3.5 h-3.5 ml-1" />
            </Link>
          </div>
          <div className="h-60 w-full">
            {sugarChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={sugarChartData}>
                  <defs>
                    <linearGradient id="colorGlucose" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="time" stroke="#94a3b8" fontSize={11} />
                  <YAxis domain={[60, 160]} stroke="#94a3b8" fontSize={11} />
                  <Tooltip />
                  <Area type="monotone" dataKey="glucose" stroke="#0ea5e9" fillOpacity={1} fill="url(#colorGlucose)" name="Glucose" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 bg-slate-50/60 rounded-xl border border-dashed border-slate-200">
                <Droplet className="w-8 h-8 text-slate-300 mb-2" />
                <p className="text-sm font-semibold text-slate-700">No Blood Sugar Readings Yet</p>
                <p className="text-xs text-slate-400 mt-1 max-w-xs">
                  Track fasting or post-meal glucose in the Health Tracker to monitor metabolic stability.
                </p>
                <Link to="/health-tracker" className="mt-3 px-3 py-1.5 bg-sky-50 hover:bg-sky-100 text-sky-700 font-semibold rounded-lg text-xs">
                  + Log Blood Sugar
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Today's Full Dosage Schedule List */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-bold text-slate-900">Today's Medication Doses</h3>
          <Link to="/reminders" className="text-xs font-semibold text-brand-600 hover:text-brand-700">
            Manage All
          </Link>
        </div>

        <div className="divide-y divide-slate-100">
          {(today?.todayDoses || []).length > 0 ? (
            today.todayDoses.map((dose) => (
              <div key={dose.id} className="py-3.5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
                <div className="flex items-center space-x-3">
                  <div className={`w-9 h-9 rounded-xl flex items-center justify-center font-bold text-xs ${
                    dose.status === 'TAKEN'
                      ? 'bg-emerald-100 text-emerald-700'
                      : dose.status === 'MISSED'
                      ? 'bg-rose-100 text-rose-700'
                      : 'bg-brand-50 text-brand-700'
                  }`}>
                    {dose.status === 'TAKEN' ? '✓' : dose.status === 'MISSED' ? '!' : '⏰'}
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-slate-800">{dose.medicineName}</h4>
                    <span className="text-xs text-slate-400">Scheduled: {dose.scheduledTime?.substring(11, 16)}</span>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                    dose.status === 'TAKEN'
                      ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                      : dose.status === 'SKIPPED'
                      ? 'bg-slate-100 text-slate-600'
                      : dose.status === 'MISSED'
                      ? 'bg-rose-50 text-rose-700 border border-rose-200'
                      : 'bg-amber-50 text-amber-700 border border-amber-200'
                  }`}>
                    {dose.status}
                  </span>

                  {(dose.status === 'SCHEDULED' || dose.status === 'SNOOZED') && (
                    <div className="flex space-x-1.5">
                      <button
                        onClick={() => handleTakeDose(dose.id)}
                        className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-medium"
                      >
                        Take
                      </button>
                      <button
                        onClick={() => handleSkipDose(dose.id)}
                        className="px-2 py-1 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-lg text-xs"
                      >
                        Skip
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))
          ) : (
            <div className="text-center py-10 px-4 bg-slate-50/50 rounded-xl border border-dashed border-slate-200">
              <Pill className="w-8 h-8 text-slate-300 mx-auto mb-2" />
              <h4 className="text-sm font-semibold text-slate-700">
                {data?.activeMedicinesCount === 0
                  ? 'No medications added yet'
                  : 'No remaining doses scheduled for today'}
              </h4>
              <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
                {data?.activeMedicinesCount === 0
                  ? 'Add your daily prescriptions to automatically generate your reminder schedule with Take, Skip, and Snooze actions.'
                  : 'All doses for today have been acknowledged or none were scheduled for this time window.'}
              </p>
              {data?.activeMedicinesCount === 0 && (
                <Link
                  to="/medicines/new"
                  className="mt-3 inline-flex items-center space-x-1.5 px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white font-semibold rounded-xl text-xs shadow-sm"
                >
                  <span>+ Add Your First Medicine</span>
                </Link>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
