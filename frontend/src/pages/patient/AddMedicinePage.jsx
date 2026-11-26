import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../../api/axiosClient';
import { Pill, Plus, ArrowLeft, Clock } from 'lucide-react';

export default function AddMedicinePage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    genericName: '',
    medicineType: 'TABLET',
    strength: '',
    dosageInstructions: 'Take with plenty of water',
    beforeAfterFood: 'AFTER_FOOD',
    startDate: new Date().toISOString().substring(0, 10),
    endDate: '',
    prescribingDoctor: '',
    initialStock: 30,
    minimumThreshold: 5,
    notes: '',
    // Reminder Schedule options
    createSchedule: true,
    reminderTimes: ['08:00', '20:00']
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  const handleAddTime = () => {
    setFormData({
      ...formData,
      reminderTimes: [...formData.reminderTimes, '14:00']
    });
  };

  const handleTimeChange = (index, val) => {
    const updated = [...formData.reminderTimes];
    updated[index] = val;
    setFormData({ ...formData, reminderTimes: updated });
  };

  const handleRemoveTime = (index) => {
    const updated = formData.reminderTimes.filter((_, i) => i !== index);
    setFormData({ ...formData, reminderTimes: updated });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // 1. Add Medicine
      const medRes = await axiosClient.post('/api/v1/medicines', {
        name: formData.name,
        genericName: formData.genericName,
        medicineType: formData.medicineType,
        strength: formData.strength,
        dosageInstructions: formData.dosageInstructions,
        beforeAfterFood: formData.beforeAfterFood,
        startDate: formData.startDate,
        endDate: formData.endDate || null,
        prescribingDoctor: formData.prescribingDoctor,
        initialStock: parseInt(formData.initialStock, 10),
        minimumThreshold: parseInt(formData.minimumThreshold, 10),
        notes: formData.notes
      });

      // 2. If configured, create Reminder Schedule
      if (formData.createSchedule && medRes?.data?.id && formData.reminderTimes.length > 0) {
        await axiosClient.post('/api/v1/schedules', {
          medicineId: medRes.data.id,
          medicineName: formData.name,
          dosageQuantity: '1',
          frequencyType: 'DAILY',
          startDate: formData.startDate,
          endDate: formData.endDate || null,
          reminderTimes: formData.reminderTimes.map(t => t.length === 5 ? `${t}:00` : t)
        });

        // Trigger today's dose creation
        await axiosClient.post('/api/v1/doses/generate');
      }

      navigate('/medicines');
    } catch (err) {
      alert(err.message || 'Failed to add medicine');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <button
        onClick={() => navigate('/medicines')}
        className="flex items-center space-x-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Back to Medication List</span>
      </button>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 sm:p-8 shadow-sm">
        <div className="flex items-center space-x-3 pb-6 border-b border-slate-100">
          <div className="w-10 h-10 rounded-xl bg-brand-100 text-brand-700 flex items-center justify-center">
            <Pill className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-900">Add New Prescription Medication</h1>
            <p className="text-xs text-slate-500">Record dosage details and setup automated reminders</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Brand Name *</label>
              <input
                required
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder="e.g. Paracetamol, Lisinopril"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Generic Formula</label>
              <input
                name="genericName"
                value={formData.genericName}
                onChange={handleChange}
                placeholder="e.g. Acetaminophen"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Form / Type</label>
              <select
                name="medicineType"
                value={formData.medicineType}
                onChange={handleChange}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              >
                <option value="TABLET">Tablet</option>
                <option value="CAPSULE">Capsule</option>
                <option value="SYRUP">Syrup</option>
                <option value="INJECTION">Injection</option>
                <option value="DROPS">Drops</option>
                <option value="INHALER">Inhaler</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Strength *</label>
              <input
                required
                name="strength"
                value={formData.strength}
                onChange={handleChange}
                placeholder="e.g. 500 mg, 10 ml"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Meal Timing</label>
              <select
                name="beforeAfterFood"
                value={formData.beforeAfterFood}
                onChange={handleChange}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              >
                <option value="AFTER_FOOD">After Food</option>
                <option value="BEFORE_FOOD">Before Food</option>
                <option value="WITH_FOOD">With Food</option>
                <option value="ANYTIME">Anytime</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Start Date *</label>
              <input
                required
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">End Date (optional)</label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Initial Pill Stock Count</label>
              <input
                type="number"
                name="initialStock"
                value={formData.initialStock}
                onChange={handleChange}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Low-Stock Alert Threshold</label>
              <input
                type="number"
                name="minimumThreshold"
                value={formData.minimumThreshold}
                onChange={handleChange}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-brand-500"
              />
            </div>
          </div>

          {/* Schedule Checkbox */}
          <div className="pt-4 border-t border-slate-100">
            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="createSchedule"
                name="createSchedule"
                checked={formData.createSchedule}
                onChange={handleChange}
                className="rounded border-slate-300 text-brand-600 focus:ring-brand-500"
              />
              <label htmlFor="createSchedule" className="text-sm font-bold text-slate-800">
                Automatically generate daily reminder alarms
              </label>
            </div>

            {formData.createSchedule && (
              <div className="mt-4 p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-3">
                <span className="text-xs font-semibold text-slate-600 block">Daily Reminder Times:</span>
                <div className="flex flex-wrap gap-2">
                  {formData.reminderTimes.map((time, idx) => (
                    <div key={idx} className="flex items-center bg-white px-3 py-1.5 rounded-lg border border-slate-200">
                      <Clock className="w-3.5 h-3.5 text-slate-400 mr-2" />
                      <input
                        type="time"
                        value={time}
                        onChange={(e) => handleTimeChange(idx, e.target.value)}
                        className="text-xs font-medium outline-none bg-transparent"
                      />
                      {formData.reminderTimes.length > 1 && (
                        <button
                          type="button"
                          onClick={() => handleRemoveTime(idx)}
                          className="ml-2 text-slate-400 hover:text-rose-600 text-xs"
                        >
                          ×
                        </button>
                      )}
                    </div>
                  ))}
                  <button
                    type="button"
                    onClick={handleAddTime}
                    className="px-3 py-1.5 rounded-lg border border-dashed border-slate-300 text-xs font-semibold text-slate-600 hover:bg-slate-100"
                  >
                    + Add Time
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="pt-4 flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => navigate('/medicines')}
              className="px-4 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-xs font-semibold shadow-sm transition-all disabled:opacity-50"
            >
              {loading ? 'Saving Medication...' : 'Save & Configure'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
