import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { FileText, Download, FileSpreadsheet, CheckCircle2, ShieldCheck } from 'lucide-react';

export default function ReportsPage() {
  const { user } = useAuth();
  const [days, setDays] = useState(30);
  const [downloading, setDownloading] = useState(false);

  const downloadPdf = () => {
    setDownloading(true);
    const url = `/api/v1/reports/pdf?days=${days}`;
    window.open(url, '_blank');
    setTimeout(() => setDownloading(false), 2000);
  };

  const downloadCsv = () => {
    setDownloading(true);
    const url = `/api/v1/reports/csv?days=${days}`;
    window.open(url, '_blank');
    setTimeout(() => setDownloading(false), 2000);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Clinical Health & Adherence Reports</h1>
        <p className="text-xs text-slate-500">Generate printable PDF summaries and CSV data exports for your physician</p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 sm:p-8 shadow-sm space-y-6">
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-2">Select Reporting Time Window:</label>
          <div className="flex space-x-3">
            {[7, 14, 30, 90].map((d) => (
              <button
                key={d}
                type="button"
                onClick={() => setDays(d)}
                className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
                  days === d
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'bg-slate-50 text-slate-700 border border-slate-200 hover:bg-slate-100'
                }`}
              >
                Past {d} Days
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 pt-4 border-t border-slate-100">
          {/* PDF Card */}
          <div className="p-6 rounded-2xl border-2 border-brand-100 bg-brand-50/40 space-y-4 flex flex-col justify-between">
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-xl bg-brand-600 text-white flex items-center justify-center">
                <FileText className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 text-base">Comprehensive PDF Clinical Report</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Includes full medication catalogs, dose adherence rate percentage breakdown, latest biometric vitals, and physician consultation signature blocks.
              </p>
            </div>
            <button
              onClick={downloadPdf}
              disabled={downloading}
              className="w-full py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-semibold rounded-xl text-xs shadow-sm flex items-center justify-center space-x-2 transition-colors disabled:opacity-50"
            >
              <Download className="w-4 h-4" />
              <span>Download PDF Report</span>
            </button>
          </div>

          {/* CSV Card */}
          <div className="p-6 rounded-2xl border-2 border-emerald-100 bg-emerald-50/40 space-y-4 flex flex-col justify-between">
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-xl bg-emerald-600 text-white flex items-center justify-center">
                <FileSpreadsheet className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 text-base">Adherence Raw Data CSV</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Export raw dosage adherence counts, scheduled vs taken logs, and current inventory levels for spreadsheet analysis in Excel or Google Sheets.
              </p>
            </div>
            <button
              onClick={downloadCsv}
              disabled={downloading}
              className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-xl text-xs shadow-sm flex items-center justify-center space-x-2 transition-colors disabled:opacity-50"
            >
              <Download className="w-4 h-4" />
              <span>Export CSV File</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
