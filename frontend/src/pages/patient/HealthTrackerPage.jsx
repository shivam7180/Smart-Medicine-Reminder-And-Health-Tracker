import React, { useState, useEffect } from 'react';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Modal from '../../components/common/Modal';
import {
  Activity,
  Plus,
  Heart,
  Droplet,
  Zap,
  Target,
  AlertTriangle,
  Scale,
  Moon,
  GlassWater
} from 'lucide-react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip
} from 'recharts';

export default function HealthTrackerPage() {
  const [vitals, setVitals] = useState({});
  const [bpTrends, setBpTrends] = useState([]);
  const [sugarTrends, setSugarTrends] = useState([]);
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [recordModalOpen, setRecordModalOpen] = useState(false);
  const [goalModalOpen, setGoalModalOpen] = useState(false);

  const [newRecord, setNewRecord] = useState({
    recordType: 'BLOOD_PRESSURE',
    valuePrimary: '',
    valueSecondary: '',
    unit: 'mmHg',
    notes: ''
  });

  const [newGoal, setNewGoal] = useState({
    goalType: 'WEIGHT',
    targetValue: '',
    unit: 'kg',
    startDate: new Date().toISOString().substring(0, 10),
    targetDate: ''
  });

  const fetchHealthData = async () => {
    try {
      const vRes = await axiosClient.get('/api/v1/health/vitals/latest');
      if (vRes?.success) setVitals(vRes.data || {});

      const bpRes = await axiosClient.get('/api/v1/health/trends?recordType=BLOOD_PRESSURE&days=14');
      if (bpRes?.success) {
        setBpTrends(bpRes.data.map(item => ({
          date: item.readingTime?.substring(5, 10),
          systolic: item.valuePrimary,
          diastolic: item.valueSecondary
        })));
      }

      const sugarRes = await axiosClient.get('/api/v1/health/trends?recordType=BLOOD_SUGAR&days=14');
      if (sugarRes?.success) {
        setSugarTrends(sugarRes.data.map(item => ({
          date: item.readingTime?.substring(5, 10),
          glucose: item.valuePrimary
        })));
      }

      const gRes = await axiosClient.get('/api/v1/goals');
      if (gRes?.success) setGoals(gRes.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealthData();
  }, []);

  const handleRecordSubmit = async (e) => {
    e.preventDefault();
    try {
      await axiosClient.post('/api/v1/health/records', {
        recordType: newRecord.recordType,
        valuePrimary: parseFloat(newRecord.valuePrimary),
        valueSecondary: newRecord.valueSecondary ? parseFloat(newRecord.valueSecondary) : null,
        unit: newRecord.unit,
        notes: newRecord.notes
      });
      setRecordModalOpen(false);
      fetchHealthData();
    } catch (err) {
      alert(err.message || 'Error recording vitals');
    }
  };

  const handleGoalSubmit = async (e) => {
    e.preventDefault();
    try {
      await axiosClient.post('/api/v1/goals', {
        goalType: newGoal.goalType,
        targetValue: parseFloat(newGoal.targetValue),
        unit: newGoal.unit,
        startDate: newGoal.startDate,
        targetDate: newGoal.targetDate || null
      });
      setGoalModalOpen(false);
      fetchHealthData();
    } catch (err) {
      alert(err.message || 'Error saving goal');
    }
  };

  if (loading) return <LoadingSpinner message="Aggregating patient biometric history..." />;

  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Health Tracker & Vitals</h1>
          <p className="text-xs text-slate-500">Monitor blood pressure, blood glucose, BMI, and personal fitness goals</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={() => setGoalModalOpen(true)}
            className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-xl text-xs font-semibold"
          >
            + Set Health Goal
          </button>
          <button
            onClick={() => setRecordModalOpen(true)}
            className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-xs font-semibold shadow-sm flex items-center space-x-1.5"
          >
            <Plus className="w-4 h-4" />
            <span>Log Vital Reading</span>
          </button>
        </div>
      </div>

      {/* Vitals Summary Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-[11px] font-semibold text-slate-400 block mb-1">Blood Pressure</span>
          <div className="text-lg font-bold text-slate-900">
            {vitals.bloodPressureSystolic ? `${vitals.bloodPressureSystolic}/${vitals.bloodPressureDiastolic}` : '120/80'}
          </div>
          <span className="text-[10px] text-slate-400">mmHg</span>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-[11px] font-semibold text-slate-400 block mb-1">Fasting Glucose</span>
          <div className="text-lg font-bold text-slate-900">{vitals.bloodSugar || 98}</div>
          <span className="text-[10px] text-slate-400">mg/dL</span>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-[11px] font-semibold text-slate-400 block mb-1">Pulse / HR</span>
          <div className="text-lg font-bold text-slate-900">{vitals.heartRate || 72}</div>
          <span className="text-[10px] text-slate-400">bpm</span>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-[11px] font-semibold text-slate-400 block mb-1">Weight</span>
          <div className="text-lg font-bold text-slate-900">{vitals.weight || 74.5}</div>
          <span className="text-[10px] text-slate-400">kg</span>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-[11px] font-semibold text-slate-400 block mb-1">Calculated BMI</span>
          <div className="text-lg font-bold text-slate-900">{vitals.bmi || 23.5}</div>
          <span className="text-[10px] text-emerald-600 font-semibold">{vitals.bmiCategory || 'Normal'}</span>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200">
          <span className="text-[11px] font-semibold text-slate-400 block mb-1">SpO2 Level</span>
          <div className="text-lg font-bold text-slate-900">{vitals.spo2 || 99}</div>
          <span className="text-[10px] text-slate-400">% room air</span>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <h3 className="font-bold text-slate-800 text-sm mb-1">Blood Pressure Trend (14 Days)</h3>
          <p className="text-xs text-slate-400 mb-4">Systolic (target: &lt;130) and Diastolic (target: &lt;80)</p>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={bpTrends.length ? bpTrends : [
                { date: '11-18', systolic: 122, diastolic: 82 },
                { date: '11-20', systolic: 124, diastolic: 83 },
                { date: '11-22', systolic: 121, diastolic: 80 },
                { date: '11-24', systolic: 122, diastolic: 82 },
                { date: '11-25', systolic: 120, diastolic: 80 },
              ]}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={11} />
                <YAxis domain={[50, 160]} stroke="#94a3b8" fontSize={11} />
                <Tooltip />
                <Line type="monotone" dataKey="systolic" stroke="#f43f5e" strokeWidth={2} name="Systolic" />
                <Line type="monotone" dataKey="diastolic" stroke="#0284c7" strokeWidth={2} name="Diastolic" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <h3 className="font-bold text-slate-800 text-sm mb-1">Blood Glucose Trend (14 Days)</h3>
          <p className="text-xs text-slate-400 mb-4">Target Fasting Range: 70 - 100 mg/dL</p>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={sugarTrends.length ? sugarTrends : [
                { date: '11-18', glucose: 104 },
                { date: '11-20', glucose: 108 },
                { date: '11-22', glucose: 112 },
                { date: '11-24', glucose: 101 },
                { date: '11-25', glucose: 98 },
              ]}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={11} />
                <YAxis domain={[60, 150]} stroke="#94a3b8" fontSize={11} />
                <Tooltip />
                <Line type="monotone" dataKey="glucose" stroke="#0ea5e9" strokeWidth={2} name="Glucose mg/dL" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Health Goals */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
        <h3 className="font-bold text-slate-800 text-sm mb-4">Active Health Goals</h3>
        {goals.length === 0 ? (
          <p className="text-xs text-slate-400 text-center py-6">No personal health goals configured.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {goals.map((g) => (
              <div key={g.id} className="p-4 rounded-xl border border-slate-200 bg-slate-50 space-y-2">
                <div className="flex justify-between items-center">
                  <span className="font-bold text-sm text-slate-800">{g.goalType}</span>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    g.status === 'ACHIEVED' ? 'bg-emerald-100 text-emerald-800' : 'bg-brand-100 text-brand-800'
                  }`}>
                    {g.status}
                  </span>
                </div>
                <div className="text-xs text-slate-500">
                  Target: <strong>{g.targetValue} {g.unit}</strong> | Current: {g.currentValue} {g.unit}
                </div>
                <div className="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
                  <div
                    className="bg-brand-600 h-full rounded-full transition-all"
                    style={{ width: `${Math.min(100, g.progressPercentage || 0)}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Log Vital Modal */}
      <Modal isOpen={recordModalOpen} onClose={() => setRecordModalOpen(false)} title="Log Health Vital Measurement">
        <form onSubmit={handleRecordSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Measurement Type</label>
            <select
              value={newRecord.recordType}
              onChange={(e) => {
                const type = e.target.value;
                let defaultUnit = 'mmHg';
                if (type === 'BLOOD_SUGAR') defaultUnit = 'mg/dL';
                else if (type === 'HEART_RATE') defaultUnit = 'bpm';
                else if (type === 'WEIGHT') defaultUnit = 'kg';
                else if (type === 'HEIGHT') defaultUnit = 'cm';
                else if (type === 'SPO2') defaultUnit = '%';
                else if (type === 'WATER_INTAKE') defaultUnit = 'ml';
                else if (type === 'SLEEP') defaultUnit = 'hours';
                setNewRecord({ ...newRecord, recordType: type, unit: defaultUnit });
              }}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
            >
              <option value="BLOOD_PRESSURE">Blood Pressure</option>
              <option value="BLOOD_SUGAR">Blood Glucose</option>
              <option value="HEART_RATE">Heart Rate</option>
              <option value="WEIGHT">Body Weight</option>
              <option value="HEIGHT">Height</option>
              <option value="SPO2">Oxygen Saturation (SpO2)</option>
              <option value="WATER_INTAKE">Daily Water Hydration</option>
              <option value="SLEEP">Sleep Hours</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                {newRecord.recordType === 'BLOOD_PRESSURE' ? 'Systolic Value' : 'Measurement Value'}
              </label>
              <input
                required
                type="number"
                step="any"
                value={newRecord.valuePrimary}
                onChange={(e) => setNewRecord({ ...newRecord, valuePrimary: e.target.value })}
                placeholder="e.g. 120"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
              />
            </div>
            {newRecord.recordType === 'BLOOD_PRESSURE' ? (
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Diastolic Value</label>
                <input
                  required
                  type="number"
                  step="any"
                  value={newRecord.valueSecondary}
                  onChange={(e) => setNewRecord({ ...newRecord, valueSecondary: e.target.value })}
                  placeholder="e.g. 80"
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
                />
              </div>
            ) : (
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Unit</label>
                <input
                  required
                  value={newRecord.unit}
                  onChange={(e) => setNewRecord({ ...newRecord, unit: e.target.value })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
                />
              </div>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Notes (optional)</label>
            <input
              value={newRecord.notes}
              onChange={(e) => setNewRecord({ ...newRecord, notes: e.target.value })}
              placeholder="e.g. Measured 30m after exercise"
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
            />
          </div>

          <div className="pt-2 flex justify-end space-x-2">
            <button
              type="button"
              onClick={() => setRecordModalOpen(false)}
              className="px-4 py-2 border rounded-xl text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2 bg-brand-600 text-white rounded-xl text-xs font-semibold"
            >
              Save Vital
            </button>
          </div>
        </form>
      </Modal>

      {/* Goal Modal */}
      <Modal isOpen={goalModalOpen} onClose={() => setGoalModalOpen(false)} title="Create Health Target Goal">
        <form onSubmit={handleGoalSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Goal Metric</label>
            <select
              value={newGoal.goalType}
              onChange={(e) => setNewGoal({ ...newGoal, goalType: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
            >
              <option value="WEIGHT">Weight (kg)</option>
              <option value="WATER_INTAKE">Daily Hydration (ml)</option>
              <option value="SLEEP">Sleep (hours)</option>
              <option value="BLOOD_PRESSURE">Systolic Target (mmHg)</option>
            </select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Target Numeric Value</label>
              <input
                required
                type="number"
                step="any"
                value={newGoal.targetValue}
                onChange={(e) => setNewGoal({ ...newGoal, targetValue: e.target.value })}
                placeholder="e.g. 70"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Unit</label>
              <input
                required
                value={newGoal.unit}
                onChange={(e) => setNewGoal({ ...newGoal, unit: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
              />
            </div>
          </div>
          <div className="pt-2 flex justify-end space-x-2">
            <button type="button" onClick={() => setGoalModalOpen(false)} className="px-4 py-2 border rounded-xl text-xs font-semibold">
              Cancel
            </button>
            <button type="submit" className="px-5 py-2 bg-brand-600 text-white rounded-xl text-xs font-semibold">
              Create Goal
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
