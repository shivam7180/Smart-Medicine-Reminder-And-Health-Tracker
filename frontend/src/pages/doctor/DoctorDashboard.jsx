import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import StatCard from '../../components/common/StatCard';
import { Calendar, Users, FileCheck2, Clock, CheckCircle } from 'lucide-react';

export default function DoctorDashboard() {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Look up doctor profile by user ID
    axiosClient.get(`/api/v1/doctors/user/${user?.id || 2}`)
      .then((docRes) => {
        if (docRes?.data?.id) {
          return axiosClient.get(`/api/v1/appointments/doctor/${docRes.data.id}`);
        }
        return { data: [] };
      })
      .then((res) => {
        setAppointments(res?.data || []);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [user]);

  const handleConfirm = async (id) => {
    await axiosClient.patch(`/api/v1/appointments/${id}/status?status=CONFIRMED&doctorNotes=Confirmed by physician`);
    window.location.reload();
  };

  const handleComplete = async (id) => {
    await axiosClient.patch(`/api/v1/appointments/${id}/status?status=COMPLETED&doctorNotes=Consultation completed successfully`);
    window.location.reload();
  };

  if (loading) return <LoadingSpinner message="Loading physician consultations..." />;

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-900">Physician Clinical Portal</h1>
        <p className="text-xs text-slate-500 mt-1">Review patient appointments, authorize prescriptions, and monitor patient adherence</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <StatCard title="Total Consultations" value={appointments.length} subtitle="Booked sessions" icon={Calendar} color="brand" />
        <StatCard title="Pending Review" value={appointments.filter(a => a.status === 'PENDING').length} subtitle="Awaiting confirmation" icon={Clock} color="amber" />
        <StatCard title="Confirmed / Completed" value={appointments.filter(a => a.status === 'CONFIRMED' || a.status === 'COMPLETED').length} subtitle="Handled appointments" icon={CheckCircle} color="green" />
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-4">
        <h2 className="text-base font-bold text-slate-900">Patient Consultation Queue</h2>
        {appointments.length === 0 ? (
          <p className="text-xs text-slate-400 text-center py-8">No appointments assigned to your calendar yet.</p>
        ) : (
          <div className="divide-y divide-slate-100">
            {appointments.map((appt) => (
              <div key={appt.id} className="py-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-3">
                <div>
                  <div className="flex items-center space-x-2">
                    <h3 className="font-bold text-sm text-slate-900">{appt.patientName || `Patient #${appt.patientId}`}</h3>
                    {appt.patientEmail && <span className="text-xs text-slate-400">({appt.patientEmail})</span>}
                  </div>
                  <p className="text-xs text-slate-600 mt-0.5">Consultation Reason: <strong className="text-slate-800">{appt.reason}</strong></p>
                  <span className="text-[11px] text-brand-600 font-medium">
                    Schedule: {appt.appointmentDate} at {appt.appointmentTime?.substring(0, 5)}
                  </span>
                </div>

                <div className="flex items-center space-x-2">
                  <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                    appt.status === 'CONFIRMED'
                      ? 'bg-emerald-100 text-emerald-800'
                      : appt.status === 'COMPLETED'
                      ? 'bg-blue-100 text-blue-800'
                      : 'bg-amber-100 text-amber-800'
                  }`}>
                    {appt.status}
                  </span>

                  {appt.status === 'PENDING' && (
                    <button
                      onClick={() => handleConfirm(appt.id)}
                      className="px-3 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-medium"
                    >
                      Confirm
                    </button>
                  )}
                  {appt.status === 'CONFIRMED' && (
                    <button
                      onClick={() => handleComplete(appt.id)}
                      className="px-3 py-1 bg-brand-600 hover:bg-brand-700 text-white rounded-lg text-xs font-medium"
                    >
                      Mark Done
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
