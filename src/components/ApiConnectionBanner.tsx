import React from 'react';
import { Wifi, WifiOff } from 'lucide-react';

interface ApiConnectionBannerProps {
  isConnected: boolean;
  isChecking?: boolean;
}

export const ApiConnectionBanner: React.FC<ApiConnectionBannerProps> = ({ isConnected }) => {
  return (
    <div className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-bold font-mono transition-all border ${
      isConnected
        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
        : 'bg-amber-50 text-amber-700 border-amber-200'
    }`}>
      <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'}`} />
      <span>{isConnected ? 'Connected' : 'Offline Mode'}</span>
    </div>
  );
};
