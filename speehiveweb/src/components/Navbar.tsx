import React, { useState, useRef, useEffect } from 'react';
import { LayoutDashboard, Megaphone, Calendar, Bell, ShieldCheck, Users, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from './StatusBadge';

interface NavbarProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
  unreadNotificationsCount?: number;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  onTabChange,
  unreadNotificationsCount = 0,
}) => {
  const { username, role, logout } = useAuth();
  const [showUserMenu, setShowUserMenu] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setShowUserMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, role: 'all' },
    { id: 'campaigns', label: 'Campaigns', icon: Megaphone, role: 'all' },
    { id: 'events', label: 'Events', icon: Calendar, role: 'all' },
    { id: 'notifications', label: 'Notifications', icon: Bell, role: 'all', badge: unreadNotificationsCount },
    { id: 'audit-logs', label: 'Audit Logs', icon: ShieldCheck, role: 'Admin' },
    { id: 'users', label: 'User Admin', icon: Users, role: 'Admin' },
  ];

  const userInitial = username ? username.charAt(0).toUpperCase() : 'A';

  return (
    <header className="glass-navbar sticky top-0 z-40 w-full px-4 sm:px-8 py-3 transition-all">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        {/* Brand Logo with Uploaded Honeycomb Logo */}
        <div className="flex items-center gap-3 cursor-pointer" onClick={() => onTabChange('dashboard')}>
          <img src="/hive_logo.png" alt="HIVE AI Logo" className="w-10 h-10 object-contain drop-shadow-sm hover:scale-105 transition-transform" />
          <div>
            <div className="flex items-center gap-2">
              <span className="font-extrabold text-xl tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-blue-700 via-indigo-700 to-purple-800 font-heading">
                Hive AI
              </span>
            </div>
            <p className="text-[10px] font-bold text-slate-500 tracking-wider uppercase hidden sm:block font-mono">
              INTELLIGENT SM AUTOMATION
            </p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="hidden md:flex items-center gap-1.5 p-1 rounded-2xl bg-slate-100/80 border border-slate-200 shadow-inner">
          {navItems
            .filter((item) => item.role === 'all' || item.role === role)
            .map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              const hasBadge = Boolean(item.badge && item.badge > 0);

              return (
                <button
                  key={item.id}
                  onClick={() => onTabChange(item.id)}
                  className={`deep-3d-press flex items-center gap-2 px-3.5 py-2 rounded-xl text-sm font-semibold transition-all ${
                    isActive
                      ? 'bg-gradient-to-b from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/25 border border-blue-400/30'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-white/60'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                  {hasBadge ? (
                    <span className="ml-1 px-1.5 py-0.2 rounded-full text-[10px] font-bold bg-amber-500 text-white shadow-sm">
                      {item.badge}
                    </span>
                  ) : null}
                </button>
              );
            })}
        </nav>

        {/* Circular Avatar Pop-out Menu */}
        <div className="relative" ref={menuRef}>
          <button
            onClick={() => setShowUserMenu(!showUserMenu)}
            className="deep-3d-press w-10 h-10 rounded-full bg-gradient-to-tr from-blue-600 to-indigo-600 text-white font-extrabold text-sm flex items-center justify-center border-2 border-white shadow-md shadow-blue-500/20 hover:scale-105 transition-transform"
            title={`Account (${username || 'User'})`}
          >
            {userInitial}
          </button>

          {/* Pop-out Dropdown Menu */}
          {showUserMenu && (
            <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-white border border-slate-200 shadow-2xl p-3 z-50 animate-slide-in space-y-3">
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-blue-600 text-white font-extrabold text-base flex items-center justify-center shadow-inner">
                  {userInitial}
                </div>
                <div>
                  <h4 className="font-extrabold text-sm text-slate-900 leading-snug">{username}</h4>
                  <div className="mt-0.5">
                    <StatusBadge status={role || 'Admin'} type="role" />
                  </div>
                </div>
              </div>

              <div className="border-t border-slate-100 pt-2">
                <button
                  onClick={() => {
                    setShowUserMenu(false);
                    logout();
                  }}
                  className="deep-3d-press w-full p-2.5 rounded-xl bg-red-50 text-red-600 hover:bg-red-100 border border-red-200 text-xs font-bold flex items-center justify-center gap-2"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Log Out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Mobile Tab Navigation */}
      <div className="md:hidden flex items-center justify-around mt-3 pt-2 border-t border-slate-200/80 overflow-x-auto">
        {navItems
          .filter((item) => item.role === 'all' || item.role === role)
          .map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            const hasBadge = Boolean(item.badge && item.badge > 0);

            return (
              <button
                key={item.id}
                onClick={() => onTabChange(item.id)}
                className={`relative flex flex-col items-center gap-1 p-1.5 rounded-lg text-[11px] font-semibold transition-all ${
                  isActive ? 'text-blue-600 font-bold' : 'text-slate-500'
                }`}
              >
                <div className="relative">
                  <Icon className="w-5 h-5" />
                  {hasBadge && (
                    <span className="absolute -top-1 -right-2 px-1 py-0.2 rounded-full text-[9px] font-bold bg-amber-500 text-white">
                      {item.badge}
                    </span>
                  )}
                </div>
                <span>{item.label}</span>
              </button>
            );
          })}
      </div>
    </header>
  );
};
