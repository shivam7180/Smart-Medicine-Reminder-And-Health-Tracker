import React, { useState, useEffect } from 'react';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Calendar, Clock, User, CheckCircle2, XCircle } from 'lucide-react';

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAppointments = async () => {
    try {
      const res = await axiosClient.get('/api/v1/appointments/patient');
      if (res?.success) setAppointments(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleCancel = async (id) => {
    if (window.confirm('Cancel this consultation appointment?')) {
      await axiosClient.patch(`/api/v1/appointments/${id}/status?status=CANCELLED`);
      fetchAppointments();
    }
  };

  if (loading) return <LoadingSpinner message="Loading appointment schedule..." />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">My Appointments</h1>
        <p className="text-xs text-slate-500">History and status of scheduled doctor consultations</p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {appointments.length === 0 ? (
          <div className="text-center py-12 text-slate-400 text-sm">
            No consultations booked yet. Visit the Doctors directory to book one.
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {appointments.map((appt) => (
              <div key={appt.id} className="p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div className="flex items-start space-x-4">
                  <div className="w-10 h-10 rounded-xl bg-brand-50 text-brand-700 flex items-center justify-center shrink-0">
                    <Calendar className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-sm">{appt.doctorName}</h3>
                    <span className="text-xs text-brand-600 block">{appt.specialization}</span>
                    <p className="text-xs text-slate-500 mt-1">
                      Reason: <span className="text-slate-700">{appt.reason}</span>
                    </p>
                    {appt.doctorNotes && (
                      <p className="text-xs text-brand-700 mt-1 bg-brand-50 p-2 rounded-lg">
                        <strong>Doctor Notes:</strong> {appt.doctorNotes}
                      </p>
                    )}
                  </div>
                </div>

                <div className="flex items-center space-x-4 text-xs">
                  <div className="text-right">
                    <div className="font-bold text-slate-800">{appt.appointmentDate}</div>
                    <div className="text-slate-400">{appt.appointmentTime?.substring(0, 5)}</div>
                  </div>

                  <span className={`px-2.5 py-1 rounded-full font-semibold ${
                    appt.status === 'CONFIRMED'
                      ? 'bg-emerald-100 text-emerald-800'
                      : appt.status === 'COMPLETED'
                      ? 'bg-blue-100 text-blue-800'
                      : appt.status === 'CANCELLED'
                      ? 'bg-rose-100 text-rose-800'
                      : 'bg-amber-100 text-amber-800'
                  }`}>
                    {appt.status}
                  </span>

                  {appt.status === 'PENDING' && (
                    <button
                      onClick={() => handleCancel(appt.id)}
                      className="px-2.5 py-1 text-rose-600 hover:bg-rose-50 rounded-lg font-semibold"
                    >
                      Cancel
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
