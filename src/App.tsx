import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import { RoleGuard } from './components/RoleGuard';
import { Navbar } from './components/Navbar';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
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

  const fetchUnreadCount = async (isMounted = true) => {
    try {
      const response = await apiClient.get('/api/Campaigns');
      if (isMounted && Array.isArray(response.data)) {
        const lastViewedTimeStr = localStorage.getItem(LAST_VIEWED_KEY);
        const parsedTime = lastViewedTimeStr ? new Date(lastViewedTimeStr).getTime() : 0;
        const lastViewedTime = isNaN(parsedTime) ? 0 : parsedTime;

        const unreadPendingCount = response.data.filter((c: any) => {
          const status = (c.status || '').toLowerCase();
          if (status !== 'generated') return false;

          const createdTime = c.createdAt ? new Date(c.createdAt).getTime() : 0;
          if (isNaN(createdTime) || createdTime === 0) return false;
          return createdTime > lastViewedTime;
        }).length;

        if (isMounted) {
          setUnreadNotificationsCount(unreadPendingCount);
        }
      }
    } catch (e) {
      if (isMounted) {
        setUnreadNotificationsCount(0);
      }
    }
  };

  useEffect(() => {
    let isMounted = true;
    if (isAuthenticated) {
      fetchUnreadCount(isMounted);
    }
    return () => {
      isMounted = false;
    };
  }, [isAuthenticated]);

  // Synchronize hash state with components
  useEffect(() => {
    if (!isAuthenticated) return;

    const handleHashChange = () => {
      const hash = window.location.hash || '#dashboard';
      const pathPart = hash.split('?')[0];
      const queryPart = hash.split('?')[1] || '';
      const params = new URLSearchParams(queryPart);

      const tab = pathPart.substring(1); // remove '#'
      const validTabs = ['dashboard', 'events', 'campaign-detail', 'notifications', 'audit-logs', 'users'];
      
      if (validTabs.includes(tab)) {
        setActiveTab(tab);
        if (tab === 'campaign-detail') {
          const id = params.get('id');
          setSelectedCampaignId(id);
        } else {
          setSelectedCampaignId(null);
        }
        if (tab === 'notifications') {
          localStorage.setItem(LAST_VIEWED_KEY, new Date().toISOString());
          setUnreadNotificationsCount(0);
        }
      } else {
        window.location.hash = '#dashboard';
      }
    };

    window.addEventListener('hashchange', handleHashChange);
    // Sync initial load
    handleHashChange();

    return () => window.removeEventListener('hashchange', handleHashChange);
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return <Login />;
  }

  const handleNavigateToCampaignDetail = (campaignId: string) => {
    const currentHash = window.location.hash;
    let fromStatus = '';
    if (currentHash.startsWith('#events')) {
      const params = new URLSearchParams(currentHash.split('?')[1] || '');
      fromStatus = params.get('status') || '';
    }
    
    if (fromStatus) {
      window.location.hash = `#campaign-detail?id=${campaignId}&fromStatus=${fromStatus}`;
    } else {
      window.location.hash = `#campaign-detail?id=${campaignId}`;
    }
  };

  const handleTabChange = (tab: string) => {
    if (tab === 'events' && window.location.hash.startsWith('#events?')) {
      return;
    }
    window.location.hash = `#${tab}`;
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
                handleTabChange(tab === 'campaigns' ? 'events' : tab);
              }
            }}
          />
        )}

        {activeTab === 'campaign-detail' && selectedCampaignId && (
          <CampaignDetail
            campaignId={selectedCampaignId}
            onBack={() => handleTabChange('events')}
          />
        )}

        {activeTab === 'events' && (
          <Events onNavigateToCampaign={handleNavigateToCampaignDetail} />
        )}

        {activeTab === 'notifications' && (
          <Notifications onNavigateToCampaign={handleNavigateToCampaignDetail} />
        )}

        {activeTab === 'audit-logs' && (
          <RoleGuard requiredRole="Admin" onFallback={() => handleTabChange('dashboard')}>
            <AuditLogs />
          </RoleGuard>
        )}

        {activeTab === 'users' && (
          <RoleGuard requiredRole="Admin" onFallback={() => handleTabChange('dashboard')}>
            <UserAdmin />
          </RoleGuard>
        )}
      </main>

      <footer className="border-t border-slate-200 py-6 bg-white text-center text-xs font-semibold text-slate-500">
        <p>🐝 BuzzHive • INTELLIGENT SM AUTOMATION</p>
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
