import React, { useState, useEffect } from 'react';
import { ShieldCheck, Search, RefreshCw, Clock, User } from 'lucide-react';
import { AuditLog } from '../types';
import { apiClient } from '../api/client';

export const AuditLogs: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchLogs = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await apiClient.get('/api/Admin/auditlogs');
      setLogs(Array.isArray(response.data) ? response.data : []);
    } catch (err: any) {
      console.error('API call to /api/Admin/auditlogs failed:', err);
      setErrorMessage(err.response?.data?.message || 'Failed to fetch audit logs from backend server.');
      setLogs([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const getActionTitle = (action: string) => {
    switch (action) {
      case 'CREATE_USER':
        return 'Created User';
      case 'ACTIVATE_USER':
        return 'Activated User';
      case 'DEACTIVATE_USER':
        return 'Deactivated User';
      case 'APPROVE_POST':
        return 'Approved Campaign';
      case 'REJECT_POST':
        return 'Rejected Campaign';
      case 'CANCEL_EVENT':
        return 'Cancelled Event';
      case 'GOOGLE_CALENDAR_UPDATED':
        return 'Calendar Updated';
      case 'CREATE_CAMPAIGN':
        return 'Created Campaign';
      case 'APPROVE_EVENT':
        return 'Approved Event';
      case 'REJECT_EVENT':
        return 'Rejected Event';
      case 'POST_CAMPAIGN':
        return 'Posted Campaign';
      default:
        return action
          ? action
              .replace(/_/g, ' ')
              .toLowerCase()
              .replace(/\b\w/g, (c) => c.toUpperCase())
          : 'Action';
    }
  };

  const getActionBadgeStyle = (action: string) => {
    switch (action) {
      case 'CREATE_USER':
      case 'GOOGLE_CALENDAR_UPDATED':
      case 'CREATE_CAMPAIGN':
        return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'ACTIVATE_USER':
      case 'APPROVE_POST':
      case 'APPROVE_EVENT':
      case 'POST_CAMPAIGN':
        return 'bg-emerald-100 text-emerald-700 border-emerald-200';
      case 'DEACTIVATE_USER':
      case 'REJECT_POST':
      case 'CANCEL_EVENT':
      case 'REJECT_EVENT':
        return 'bg-red-100 text-red-700 border-red-200';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  const filteredLogs = logs.filter((log) => {
    const action = log.action || '';
    const userStr = log.username || log.userId || '';
    const details = log.details || '';
    const actionTitle = getActionTitle(action);

    return (
      action.toLowerCase().includes(searchQuery.toLowerCase()) ||
      actionTitle.toLowerCase().includes(searchQuery.toLowerCase()) ||
      userStr.toLowerCase().includes(searchQuery.toLowerCase()) ||
      details.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Header Bar matching AuditLogScreen.kt */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-3 font-heading">
            <ShieldCheck className="w-8 h-8 text-purple-600" />
            <span>System Audit Logs</span>
          </h1>
          <p className="text-sm font-medium text-slate-500 mt-1">
            Real-time compliance monitoring of user administrative actions and role activities
          </p>
        </div>

        <button onClick={fetchLogs} className="deep-3d-press btn-secondary text-xs">
          <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh Logs
        </button>
      </div>

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 text-xs font-bold border border-red-200">
          {errorMessage}
        </div>
      )}

      {/* Search Bar */}
      <div className="deep-3d-card p-4 bg-white/90">
        <div className="relative">
          <Search className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search audit logs by action, username, or details..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input-field pl-11"
          />
        </div>
      </div>

      {/* Audit Log Cards (Parity with AuditLogCard in Compose) */}
      {isLoading ? (
        <div className="p-12 text-center">
          <div className="w-10 h-10 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-sm font-semibold text-slate-600">Loading audit logs from backend...</p>
        </div>
      ) : filteredLogs.length === 0 ? (
        <div className="deep-3d-card p-12 text-center bg-white/90">
          <ShieldCheck className="w-12 h-12 text-slate-400 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-800">No audit logs found on backend</h3>
          <p className="text-xs text-slate-500 mt-1">0 logs returned by GET /api/Admin/auditlogs.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredLogs.map((log) => {
            const actionTitle = getActionTitle(log.action);
            const badgeStyle = getActionBadgeStyle(log.action);

            return (
              <div
                key={log.id}
                className="deep-3d-card p-5 bg-white space-y-3 border border-slate-200 hover:border-purple-300 transition-all"
              >
                <div className="flex items-center justify-between">
                  <span className={`text-xs font-bold px-3 py-1 rounded-lg border ${badgeStyle}`}>
                    {actionTitle}
                  </span>

                  <span className="text-xs font-semibold text-slate-400 flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" />
                    {log.createdAt ? new Date(log.createdAt).toLocaleString() : log.timestamp || 'Recent'}
                  </span>
                </div>

                <p className="text-sm font-medium text-slate-800 leading-relaxed">{log.details}</p>

                {log.username && (
                  <div className="pt-2 border-t border-slate-100 text-xs font-bold text-slate-500 flex items-center gap-1.5">
                    <User className="w-3.5 h-3.5 text-slate-400" />
                    <span>Performed by: {log.username}</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
