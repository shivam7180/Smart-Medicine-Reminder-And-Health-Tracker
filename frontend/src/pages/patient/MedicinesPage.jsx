import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Pill, Plus, Search, AlertCircle, Trash2, Edit3, ShieldAlert } from 'lucide-react';

export default function MedicinesPage() {
  const [medicines, setMedicines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');

  const fetchMedicines = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get(`/api/v1/medicines?keyword=${keyword}`);
      if (res?.success) {
        setMedicines(res.data?.content || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMedicines();
  }, [keyword]);

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this medication?')) {
      try {
        await axiosClient.delete(`/api/v1/medicines/${id}`);
        fetchMedicines();
      } catch (err) {
        alert(err.message || 'Error deleting medicine');
      }
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">My Medications</h1>
          <p className="text-xs text-slate-500">Track active medicines, dosages, and pharmacy inventory</p>
        </div>
        <Link
          to="/medicines/new"
          className="px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-xs font-semibold shadow-sm flex items-center space-x-2"
        >
          <Plus className="w-4 h-4" />
          <span>Add New Medicine</span>
        </Link>
      </div>

      {/* Search Bar */}
      <div className="bg-white p-3 rounded-2xl border border-slate-200 flex items-center space-x-3">
        <Search className="w-4 h-4 text-slate-400 ml-2" />
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Search by brand name or generic formula (e.g. Lisinopril, Metformin)..."
          className="w-full text-sm outline-none bg-transparent"
        />
      </div>

      {loading ? (
        <LoadingSpinner message="Fetching medicines catalog..." />
      ) : medicines.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center space-y-3">
          <Pill className="w-10 h-10 text-slate-300 mx-auto" />
          <h3 className="text-base font-bold text-slate-700">No Medications Found</h3>
          <p className="text-xs text-slate-500">Add your daily prescriptions to begin receiving automated dosage reminders.</p>
          <Link to="/medicines/new" className="inline-block px-4 py-2 bg-brand-600 text-white text-xs font-semibold rounded-xl">
            Add First Medicine
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {medicines.map((med) => (
            <div key={med.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm hover:shadow-md transition-shadow relative">
              <div className="flex justify-between items-start">
                <div className="flex items-start space-x-3">
                  <div className="w-10 h-10 rounded-xl bg-brand-50 text-brand-700 flex items-center justify-center shrink-0">
                    <Pill className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-base">{med.name}</h3>
                    <p className="text-xs text-slate-400">{med.genericName || 'No generic name'}</p>
                  </div>
                </div>
                <button
                  onClick={() => handleDelete(med.id)}
                  className="text-slate-400 hover:text-rose-600 transition-colors p-1"
                  title="Delete"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 space-y-1.5 text-xs text-slate-600">
                <div className="flex justify-between">
                  <span className="text-slate-400">Dosage Strength:</span>
                  <span className="font-semibold text-slate-800">{med.strength}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Type:</span>
                  <span className="font-semibold text-slate-800">{med.medicineType}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Instructions:</span>
                  <span className="font-semibold text-slate-800">{med.beforeAfterFood || 'After Food'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Current Stock:</span>
                  <span className={`font-semibold ${med.isLowStock ? 'text-rose-600' : 'text-slate-800'}`}>
                    {med.currentStock || 0} units
                  </span>
                </div>
              </div>

              {med.isLowStock && (
                <div className="mt-3 p-2 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center space-x-1.5 font-medium">
                  <ShieldAlert className="w-4 h-4 shrink-0" />
                  <span>Low stock warning! Threshold: {med.minimumThreshold}</span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
