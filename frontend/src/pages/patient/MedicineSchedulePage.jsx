import React, { useState, useEffect } from 'react';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { CalendarCheck, Clock, Plus, Trash2, CheckCircle2 } from 'lucide-react';

export default function MedicineSchedulePage() {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchSchedules = async () => {
    try {
      const res = await axiosClient.get('/api/v1/schedules/active');
      if (res?.success) setSchedules(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSchedules();
  }, []);

  const handleDelete = async (id) => {
    if (window.confirm('Delete this reminder schedule?')) {
      await axiosClient.delete(`/api/v1/schedules/${id}`);
      fetchSchedules();
    }
  };

  if (loading) return <LoadingSpinner message="Loading schedules..." />;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Medicine Schedules</h1>
          <p className="text-xs text-slate-500">Recurring daily and weekly dosage timing rules</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {schedules.map((s) => (
          <div key={s.id} className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-bold text-slate-900">{s.medicineName}</h3>
                <span className="text-xs text-brand-600 font-semibold">{s.frequencyType} Schedule</span>
              </div>
              <button onClick={() => handleDelete(s.id)} className="text-slate-400 hover:text-rose-600 p-1">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-2 text-xs text-slate-600 pt-2 border-t border-slate-100">
              <div className="flex justify-between">
                <span className="text-slate-400">Dosage Quantity:</span>
                <span className="font-semibold text-slate-800">{s.dosageQuantity}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Duration:</span>
                <span className="font-semibold text-slate-800">
                  {s.startDate} to {s.endDate || 'Ongoing'}
                </span>
              </div>
            </div>

            <div className="pt-2">
              <span className="text-xs font-semibold text-slate-400 block mb-2 uppercase tracking-wider">
                Configured Daily Times:
              </span>
              <div className="flex flex-wrap gap-2">
                {(s.reminderTimes || []).map((t, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1 rounded-lg bg-brand-50 border border-brand-200 text-brand-700 text-xs font-bold flex items-center space-x-1"
                  >
                    <Clock className="w-3 h-3 mr-1" />
                    <span>{t.substring(0, 5)}</span>
                  </span>
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
