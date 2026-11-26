import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../../api/axiosClient';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Modal from '../../components/common/Modal';
import { UserCheck, Calendar, DollarSign, Award, ArrowRight } from 'lucide-react';

export default function DoctorsPage() {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  const [booking, setBooking] = useState({
    appointmentDate: new Date(Date.now() + 86400000).toISOString().substring(0, 10),
    appointmentTime: '10:00',
    reason: 'Routine health check and medication dosage review'
  });

  const fetchDoctors = async () => {
    try {
      const res = await axiosClient.get('/api/v1/doctors');
      if (res?.success) setDoctors(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDoctors();
  }, []);

  const handleBookSubmit = async (e) => {
    e.preventDefault();
    try {
      await axiosClient.post('/api/v1/appointments', {
        doctorId: selectedDoctor.id,
        appointmentDate: booking.appointmentDate,
        appointmentTime: booking.appointmentTime.length === 5 ? `${booking.appointmentTime}:00` : booking.appointmentTime,
        reason: booking.reason
      });
      setModalOpen(false);
      alert('Appointment successfully booked! You can view status under Appointments.');
    } catch (err) {
      alert(err.message || 'Error booking appointment');
    }
  };

  if (loading) return <LoadingSpinner message="Loading doctors directory..." />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Find a Specialist Doctor</h1>
        <p className="text-xs text-slate-500">Schedule appointments and share your clinical vitals with medical physicians</p>
      </div>

      {doctors.length === 0 ? (
        <div className="bg-white rounded-3xl border border-slate-200 p-12 text-center max-w-xl mx-auto space-y-3 shadow-sm">
          <div className="w-16 h-16 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center mx-auto">
            <UserCheck className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-bold text-slate-800">No Doctors Available</h3>
          <p className="text-xs text-slate-500 leading-relaxed max-w-md mx-auto">
            There are currently no active doctors available in the directory. Once a doctor registers their clinical practice on MediCare, their profile, specialization, and consultation slots will be displayed here for booking.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {doctors.map((doc) => (
            <div key={doc.id} className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-shadow space-y-4">
              <div className="flex items-start space-x-3">
                <div className="w-12 h-12 rounded-xl bg-brand-100 text-brand-700 font-bold flex items-center justify-center shrink-0">
                  {doc.fullName.charAt(0)}
                </div>
                <div>
                  <div className="flex items-center space-x-2">
                    <h3 className="font-bold text-slate-900 text-base">{doc.fullName}</h3>
                    {doc.isRegisteredUser && (
                      <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 text-[10px] font-bold uppercase tracking-wider">
                        Live Registered
                      </span>
                    )}
                  </div>
                  <span className="text-xs font-semibold text-brand-600 block">{doc.specializationName}</span>
                  <span className="text-[11px] text-slate-400">{doc.qualification}</span>
                </div>
              </div>

              <p className="text-xs text-slate-600 line-clamp-2">{doc.bio}</p>

              <div className="pt-3 border-t border-slate-100 space-y-1.5 text-xs text-slate-600">
                <div className="flex justify-between">
                  <span className="text-slate-400">Experience:</span>
                  <span className="font-semibold">{doc.experienceYears} Years</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Consultation Fee:</span>
                  <span className="font-semibold text-slate-900">${doc.consultationFee}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Clinic Days:</span>
                  <span className="font-semibold text-slate-700">{doc.availableDays}</span>
                </div>
              </div>

              <button
                onClick={() => {
                  setSelectedDoctor(doc);
                  setModalOpen(true);
                }}
                className="w-full py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-xs font-semibold shadow-sm transition-colors flex items-center justify-center space-x-1"
              >
                <Calendar className="w-4 h-4 mr-1" />
                <span>Book Appointment</span>
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Booking Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={`Book Appointment with ${selectedDoctor?.fullName || 'Doctor'}`}
      >
        <form onSubmit={handleBookSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Appointment Date</label>
            <input
              required
              type="date"
              value={booking.appointmentDate}
              onChange={(e) => setBooking({ ...booking, appointmentDate: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Time Slot</label>
            <input
              required
              type="time"
              value={booking.appointmentTime}
              onChange={(e) => setBooking({ ...booking, appointmentTime: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Reason for Consultation</label>
            <textarea
              required
              rows="3"
              value={booking.reason}
              onChange={(e) => setBooking({ ...booking, reason: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-xl"
            ></textarea>
          </div>

          <div className="pt-2 flex justify-end space-x-2">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-4 py-2 border rounded-xl text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2 bg-brand-600 text-white rounded-xl text-xs font-semibold shadow-sm"
            >
              Confirm Booking
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
