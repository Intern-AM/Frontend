import React from 'react';
import { Wifi, WifiOff, Globe } from 'lucide-react';
import { SERVER_ORIGIN } from '../api/client';

interface ApiConnectionBannerProps {
  isConnected: boolean;
  isChecking?: boolean;
}

export const ApiConnectionBanner: React.FC<ApiConnectionBannerProps> = ({ isConnected }) => {
  return (
    <div className={`w-full px-4 py-2.5 rounded-2xl text-xs font-semibold flex items-center justify-between shadow-sm transition-colors ${
      isConnected
        ? 'bg-emerald-600 text-white shadow-emerald-500/20'
        : 'bg-gradient-to-r from-amber-600 to-orange-600 text-white shadow-amber-500/20'
    }`}>
      <div className="max-w-7xl mx-auto w-full flex items-center justify-between">
        <div className="flex items-center gap-2">
          {isConnected ? <Wifi className="w-4 h-4" /> : <WifiOff className="w-4 h-4 animate-pulse" />}
          <span>
            {isConnected
              ? `Connected to Live Backend API (${SERVER_ORIGIN})`
              : `Backend API Gateway Active (${SERVER_ORIGIN})`}
          </span>
        </div>
        <div className="flex items-center gap-1.5 text-[11px] opacity-95">
          <Globe className="w-3.5 h-3.5" />
          <span>Tailscale HTTPS Port 8443</span>
        </div>
      </div>
    </div>
  );
};
