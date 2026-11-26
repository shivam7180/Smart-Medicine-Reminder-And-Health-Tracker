import React, { useState } from 'react';
import Navbar from '../../components/layout/Navbar';
import { Mail, Phone, MapPin, CheckCircle } from 'lucide-react';

export default function ContactPage() {
  const [submitted, setSubmitted] = useState(false);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-16">
        <h1 className="text-3xl font-extrabold text-slate-900 mb-6 text-center">Contact Support</h1>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="font-bold text-slate-800 text-lg">Hospital & Platform Support</h3>
            <div className="flex items-center space-x-3 text-slate-600 text-sm">
              <Mail className="w-5 h-5 text-brand-600" />
              <span>support@medicare.com</span>
            </div>
            <div className="flex items-center space-x-3 text-slate-600 text-sm">
              <Phone className="w-5 h-5 text-brand-600" />
              <span>+1 (800) 555-0199</span>
            </div>
            <div className="flex items-center space-x-3 text-slate-600 text-sm">
              <MapPin className="w-5 h-5 text-brand-600" />
              <span>Medical Center Plaza, Suite 400</span>
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            {submitted ? (
              <div className="text-center py-8 space-y-2">
                <CheckCircle className="w-10 h-10 text-emerald-500 mx-auto" />
                <h4 className="font-bold text-slate-800">Message Delivered</h4>
                <p className="text-xs text-slate-500">Our medical support team will respond shortly.</p>
              </div>
            ) : (
              <form onSubmit={(e) => { e.preventDefault(); setSubmitted(true); }} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Your Name</label>
                  <input required className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm" placeholder="John Doe" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Email</label>
                  <input required type="email" className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm" placeholder="john@example.com" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Message</label>
                  <textarea required rows="3" className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm" placeholder="How can we help you?"></textarea>
                </div>
                <button type="submit" className="w-full py-2 bg-brand-600 hover:bg-brand-700 text-white font-semibold rounded-lg text-sm transition-colors">
                  Submit Query
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
