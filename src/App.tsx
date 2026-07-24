import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import { Navbar } from './components/Navbar';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Campaigns } from './pages/Campaigns';
import { CampaignDetail } from './pages/CampaignDetail';
import { Events } from './pages/Events';
import { Notifications } from './pages/Notifications';
import { AuditLogs } from './pages/AuditLogs';
import { UserAdmin } from './pages/UserAdmin';
import { apiClient } from './api/client';

const LAST_VIEWED_KEY = 'hive_last_viewed_notifications_timestamp';

const MainAppContent: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [activeTab, setActiveTab] = useState<string>('dashboard');
  const [selectedCampaignId, setSelectedCampaignId] = useState<string | null>(null);
  const [unreadNotificationsCount, setUnreadNotificationsCount] = useState<number>(0);

  const fetchUnreadCount = async () => {
    try {
      const response = await apiClient.get('/api/Campaigns');
      if (Array.isArray(response.data)) {
        const lastViewedTimeStr = localStorage.getItem(LAST_VIEWED_KEY);
        const lastViewedTime = lastViewedTimeStr ? new Date(lastViewedTimeStr).getTime() : 0;

        const unreadPendingCount = response.data.filter((c: any) => {
          const status = (c.status || '').toLowerCase();
          if (status !== 'generated') return false;

          const createdTime = c.createdAt ? new Date(c.createdAt).getTime() : 0;
          return createdTime > lastViewedTime;
        }).length;

        setUnreadNotificationsCount(unreadPendingCount);
      }
    } catch (e) {
      setUnreadNotificationsCount(0);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchUnreadCount();
    }
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return <Login />;
  }

  const handleNavigateToCampaignDetail = (campaignId: string) => {
    setSelectedCampaignId(campaignId);
    setActiveTab('campaign-detail');
  };

  const handleTabChange = (tab: string) => {
    setActiveTab(tab);
    if (tab !== 'campaign-detail') {
      setSelectedCampaignId(null);
    }
    if (tab === 'notifications') {
      localStorage.setItem(LAST_VIEWED_KEY, new Date().toISOString());
      setUnreadNotificationsCount(0);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Navbar
        activeTab={activeTab}
        onTabChange={handleTabChange}
        unreadNotificationsCount={unreadNotificationsCount}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-8 pt-6 pb-16">
        {activeTab === 'dashboard' && (
          <Dashboard
            onNavigate={(tab, campaignId) => {
              if (campaignId) {
                handleNavigateToCampaignDetail(campaignId);
              } else {
                handleTabChange(tab);
              }
            }}
          />
        )}

        {activeTab === 'campaigns' && (
          <Campaigns onSelectCampaign={handleNavigateToCampaignDetail} />
        )}

        {activeTab === 'campaign-detail' && selectedCampaignId && (
          <CampaignDetail
            campaignId={selectedCampaignId}
            onBack={() => handleTabChange('campaigns')}
          />
        )}

        {activeTab === 'events' && (
          <Events onNavigateToCampaign={handleNavigateToCampaignDetail} />
        )}

        {activeTab === 'notifications' && (
          <Notifications onNavigateToCampaign={handleNavigateToCampaignDetail} />
        )}

        {activeTab === 'audit-logs' && <AuditLogs />}

        {activeTab === 'users' && <UserAdmin />}
      </main>

      <footer className="border-t border-slate-200 py-6 bg-white text-center text-xs font-semibold text-slate-500">
        <p>🐝 Hive AI • INTELLIGENT SM AUTOMATION</p>
      </footer>
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <ErrorBoundary>
      <ToastProvider>
        <AuthProvider>
          <MainAppContent />
        </AuthProvider>
      </ToastProvider>
    </ErrorBoundary>
  );
};
