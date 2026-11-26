import React from 'react';
import Navbar from '../../components/layout/Navbar';
import { HeartPulse, CheckCircle } from 'lucide-react';

export default function AboutPage() {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-16">
        <h1 className="text-3xl font-extrabold text-slate-900 mb-6">About MediCare</h1>
        <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6 text-slate-600 leading-relaxed">
          <p>
            <strong>MediCare</strong> was engineered to solve one of the most persistent challenges in modern healthcare: <em>medication non-adherence</em>. Studies show that over 50% of prescribed medications are taken incorrectly, skipped, or forgotten, leading to preventable complications and hospitalizations.
          </p>
          <p>
            Built using enterprise Java Spring Boot microservices, Spring Cloud Gateway, Eureka Service Discovery, isolated MySQL databases per service, and a responsive React frontend, MediCare provides a secure, zero-trust healthcare infrastructure.
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-slate-100">
            <div className="flex items-start space-x-3">
              <CheckCircle className="w-5 h-5 text-brand-600 mt-0.5" />
              <span className="text-sm">High-precision Spring Scheduler dose engine</span>
            </div>
            <div className="flex items-start space-x-3">
              <CheckCircle className="w-5 h-5 text-brand-600 mt-0.5" />
              <span className="text-sm">Database-per-service isolation</span>
            </div>
            <div className="flex items-start space-x-3">
              <CheckCircle className="w-5 h-5 text-brand-600 mt-0.5" />
              <span className="text-sm">Multi-tenant role boundaries (Patient/Doctor/Admin)</span>
            </div>
            <div className="flex items-start space-x-3">
              <CheckCircle className="w-5 h-5 text-brand-600 mt-0.5" />
              <span className="text-sm">Real-time OpenFeign inter-service resilience</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
