import React from 'react';
import Navbar from '../../components/layout/Navbar';
import { Pill, Clock, Activity, ShieldCheck, FileText, Bell } from 'lucide-react';

export default function FeaturesPage() {
  const features = [
    { title: 'Medicine Management', desc: 'Maintain complete medicine inventories, batch tracking, dosage instructions, and low-stock alarms.', icon: Pill },
    { title: 'Smart Dose Reminders', desc: 'Take, Skip, and Snooze actions automatically update real-time adherence rates and deduct inventory.', icon: Clock },
    { title: 'Biometrics Tracker', desc: 'Log Blood Pressure, Glucose, SpO2, Heart Rate, Temperature, Sleep, and automated BMI calculations.', icon: Activity },
    { title: 'Role-Based Access', desc: 'Secure JWT authentication with dedicated views for Patients, Licensed Doctors, and Platform Admins.', icon: ShieldCheck },
    { title: 'Doctor Consultations', desc: 'Search specialist physicians by clinical department, check fee rates, and schedule online appointments.', icon: FileText },
    { title: 'Multichannel Alerts', desc: 'In-app notifications and email alerts ensure you never miss a vital dosage time.', icon: Bell }
  ];

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 py-16">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h1 className="text-3xl font-extrabold text-slate-900">MediCare Platform Capabilities</h1>
          <p className="mt-2 text-slate-600">Enterprise healthcare management with microservices scalability.</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((f, i) => {
            const Icon = f.icon;
            return (
              <div key={i} className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div className="w-10 h-10 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center mb-4">
                  <Icon className="w-5 h-5" />
                </div>
                <h3 className="text-lg font-bold text-slate-800 mb-2">{f.title}</h3>
                <p className="text-sm text-slate-600">{f.desc}</p>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
