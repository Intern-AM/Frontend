import React, { ReactNode } from 'react';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { UserRole } from '../types';

interface RoleGuardProps {
  requiredRole: UserRole;
  children: ReactNode;
  onFallback?: () => void;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ requiredRole, children, onFallback }) => {
  const { role } = useAuth();

  const isAuthorized = role === requiredRole || role === 'Admin';

  if (!isAuthorized) {
    return (
      <div className="p-8 text-center deep-3d-card bg-white/95 max-w-lg mx-auto my-12 space-y-4 border border-red-200 shadow-xl">
        <div className="w-14 h-14 rounded-2xl bg-red-100 text-red-600 flex items-center justify-center mx-auto shadow-md">
          <ShieldAlert className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-extrabold text-slate-900 font-heading">Access Restricted</h2>
        <p className="text-xs text-slate-600 leading-relaxed font-medium">
          You need <span className="font-bold text-red-700">{requiredRole}</span> permissions to view this section.
          Your current account role is <span className="font-bold text-slate-800">{role || 'Reviewer'}</span>.
        </p>
        <button
          onClick={onFallback || (() => window.location.reload())}
          className="deep-3d-press btn-primary text-xs mx-auto font-bold flex items-center gap-2 shadow-md shadow-blue-500/25"
        >
          <ArrowLeft className="w-4 h-4" />
          Return to Dashboard
        </button>
      </div>
    );
  }

  return <>{children}</>;
};
