import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import {
  HeartPulse,
  Clock,
  ShieldCheck,
  TrendingUp,
  BellRing,
  FileCheck2,
  Calendar,
  CheckCircle2,
  ArrowRight
} from 'lucide-react';

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-16 pb-24 lg:pt-24 lg:pb-32">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto space-y-6">
            <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-semibold">
              <span className="flex h-2 w-2 rounded-full bg-brand-500 animate-ping"></span>
              <span>Healthcare Management Platform</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight leading-[1.15]">
              Intelligent Medicine Reminders & <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-600 to-sky-500">Continuous Health Vitals</span>
            </h1>

            <p className="text-lg text-slate-600 leading-relaxed">
              MediCare unites automated dosage schedules, zero-miss dose notifications, interactive biometric health graphs (BP, blood sugar, heart rate, BMI), and direct doctor appointments in one clinical-grade ecosystem.
            </p>

            <div className="flex flex-col sm:flex-row justify-center items-center gap-4 pt-4">
              <Link
                to="/register"
                className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-semibold shadow-lg shadow-brand-500/25 transition-all flex items-center justify-center space-x-2"
              >
                <span>Get Started Free</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/login"
                className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-white hover:bg-slate-50 text-slate-700 font-semibold border border-slate-200 shadow-sm transition-all text-center"
              >
                Sign In to Dashboard
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Key Pillars Section */}
      <section className="py-16 bg-white border-y border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-2xl sm:text-3xl font-bold text-slate-900">Enterprise Healthcare Features</h2>
            <p className="mt-2 text-slate-500 text-sm">Everything patients, doctors, and clinics need for medication adherence.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 hover:border-brand-300 transition-all">
              <div className="w-12 h-12 rounded-xl bg-brand-100 text-brand-700 flex items-center justify-center mb-4">
                <Clock className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Automated Dose Scheduling</h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                Configure frequency, start/end dates, before/after meals, and multiple daily reminder slots. Live Take, Skip, and Snooze actions track adherence in real-time.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 hover:border-brand-300 transition-all">
              <div className="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-4">
                <TrendingUp className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Biometrics & Analytics</h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                Log and monitor Blood Pressure, Fasting Glucose, Heart Rate, SpO2, Temperature, and automated BMI with abnormal threshold warning alerts.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 hover:border-brand-300 transition-all">
              <div className="w-12 h-12 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center mb-4">
                <FileCheck2 className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Clinical PDF & CSV Reports</h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                Instantly download physician-ready PDF reports with dosage history, adherence percentage, active prescription catalogs, and vitals trend charts.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="mt-auto bg-slate-900 text-slate-400 py-10 text-center text-xs">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row justify-between items-center space-y-4 sm:space-y-0">
          <div>© 2026 MediCare Platform. Enterprise Healthcare & Microservices Architecture.</div>
          <div className="flex space-x-6 text-slate-300">
            <Link to="/about" className="hover:text-white">About</Link>
            <Link to="/features" className="hover:text-white">Features</Link>
            <Link to="/contact" className="hover:text-white">Support</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
