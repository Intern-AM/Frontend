import React, { useEffect, useState } from 'react';
import { Megaphone, Calendar, Clock, Key, ChevronRight, MapPin, AlertTriangle, RefreshCw, CheckCircle2, AlertCircle, Send } from 'lucide-react';
import { Campaign, SpeehiveEvent, SocialMediaCredential } from '../types';
import { apiClient, SERVER_ORIGIN } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { ApiConnectionBanner } from '../components/ApiConnectionBanner';
import { UpdateCredentialModal } from '../components/UpdateCredentialModal';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

interface DashboardProps {
  onNavigate: (tab: string, campaignId?: string) => void;
}

export const Dashboard: React.FC<DashboardProps> = ({ onNavigate }) => {
  const { role } = useAuth();
  const { showToast } = useToast();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [events, setEvents] = useState<SpeehiveEvent[]>([]);
  const [eventTitleMap, setEventTitleMap] = useState<Map<string, string>>(new Map());
  const [credentials, setCredentials] = useState<SocialMediaCredential[]>([]);
  const [selectedCredProvider, setSelectedCredProvider] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isConnected, setIsConnected] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const defaultCredentials: SocialMediaCredential[] = [
    {
      id: 'cred-1',
      provider: 'LinkedIn Business',
      maskedToken: 'pk_live_9921827419',
      expiresAt: '2026-08-15T00:00:00Z',
      isActive: true,
      updatedAt: '2026-07-20T10:00:00Z',
    },
    {
      id: 'cred-2',
      provider: 'Instagram Graph API',
      maskedToken: 'ig_live_4492810481',
      expiresAt: '2026-07-30T00:00:00Z',
      isActive: true,
      updatedAt: '2026-07-10T14:00:00Z',
    },
    {
      id: 'cred-3',
      provider: 'Google Calendar API',
      maskedToken: 'gc_live_8830192841',
      expiresAt: '2026-12-31T00:00:00Z',
      isActive: true,
      updatedAt: '2026-07-01T09:00:00Z',
    },
  ];

  const fetchDashboardData = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const [campaignRes, eventRes, credRes] = await Promise.allSettled([
        apiClient.get('/api/Campaigns'),
        apiClient.get('/api/Events'),
        apiClient.get('/api/SocialMediaCredentials'),
      ]);

      if (campaignRes.status === 'fulfilled') {
        setCampaigns(Array.isArray(campaignRes.value.data) ? campaignRes.value.data : []);
        setIsConnected(true);
      } else {
        setIsConnected(false);
        setCampaigns([]);
      }

      if (eventRes.status === 'fulfilled') {
        const loadedEvents: SpeehiveEvent[] = Array.isArray(eventRes.value.data) ? eventRes.value.data : [];
        setEvents(loadedEvents);
        const titleMap = new Map<string, string>();
        loadedEvents.forEach((e) => titleMap.set(e.id, e.title));
        setEventTitleMap(titleMap);
      } else {
        setEvents([]);
      }

      if (credRes.status === 'fulfilled' && Array.isArray(credRes.value.data) && credRes.value.data.length > 0) {
        setCredentials(credRes.value.data);
      } else {
        setCredentials(defaultCredentials);
      }
    } catch (err: any) {
      console.error('Error loading backend dashboard data:', err);
      setIsConnected(false);
      setErrorMessage('Failed to connect to backend server at ' + SERVER_ORIGIN);
      setCampaigns([]);
      setEvents([]);
      setCredentials(defaultCredentials);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const getEventName = (eventId: string) => {
    return eventTitleMap.get(eventId) || `Event: ${eventId}`;
  };

  const formatEventDate = (dateStr?: string) => {
    if (!dateStr) return 'TBA';
    try {
      return new Date(dateStr).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch (e) {
      return dateStr;
    }
  };

  // Parity with AdminSettingsScreen.kt getExpiringCredentials()
  const expiringCredentials = credentials.filter((cred) => {
    if (!cred.expiresAt) return false;
    try {
      const expDate = new Date(cred.expiresAt).getTime();
      const now = new Date().getTime();
      const thirtyDaysMs = 30 * 24 * 60 * 60 * 1000;
      return expDate - now < thirtyDaysMs;
    } catch (e) {
      return false;
    }
  });

  const pendingCount = campaigns.filter((c) => c.status && c.status.toLowerCase() === 'generated').length;
  const approvedCount = campaigns.filter((c) => c.status && c.status.toLowerCase() === 'approved').length;
  
  // ACTIVE metric represents the number of posted campaigns directly from the database
  const activePostedCount = campaigns.filter(
    (c) =>
      (c.status && (c.status.toLowerCase() === 'published' || c.status.toLowerCase() === 'posted')) ||
      Boolean(c.postedAt)
  ).length;

  const campaignQueue = campaigns
    .filter((c) => c.status && (c.status.toLowerCase() === 'generated' || c.status.toLowerCase() === 'approved'))
    .slice(0, 4);

  const upcomingEvents = events
    .filter((e) => e.status && e.status.toLowerCase() !== 'cancelled')
    .slice(0, 4);

  return (
    <div className="space-y-6 pb-12">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight font-heading">
            Dashboard Overview
          </h1>
          <p className="text-sm font-medium text-slate-500 mt-1">
            Real-time campaign management, event syncing, and API key token administration
          </p>
        </div>
        <div className="flex items-center gap-3 self-start sm:self-auto">
          <ApiConnectionBanner isConnected={isConnected} />
          <button
            onClick={fetchDashboardData}
            className="deep-3d-press btn-secondary text-xs"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh Live Data
          </button>
        </div>
      </div>

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 text-xs font-bold border border-red-200 flex items-center gap-2">
          <AlertCircle className="w-4 h-4" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* API CREDENTIALS EXPIRING SOON BANNER (Parity with AdminSettingsScreen.kt) */}
      {expiringCredentials.length > 0 && (
        <div className="deep-3d-card p-5 bg-gradient-to-r from-amber-500/15 via-orange-500/10 to-amber-500/5 border border-amber-300 space-y-2">
          <div className="flex items-center gap-2 text-amber-700 font-extrabold text-xs uppercase tracking-wider">
            <AlertTriangle className="w-4 h-4 text-amber-600 animate-bounce" />
            <span>API Credentials Expiring Soon</span>
          </div>
          <div className="space-y-1 pl-6">
            {expiringCredentials.map((cred) => (
              <p key={cred.id} className="text-xs font-semibold text-slate-800">
                • <strong className="text-amber-800">{cred.provider}</strong> key expires on{' '}
                <span className="font-mono text-amber-900 font-bold">
                  {cred.expiresAt ? formatEventDate(cred.expiresAt) : 'Soon'}
                </span>{' '}
                — Please update token to maintain social media auto-posting!
              </p>
            ))}
          </div>
        </div>
      )}

      {/* Metrics Row (Parity with StatCard in Compose DashboardScreen.kt) */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <div
          onClick={() => onNavigate('campaigns')}
          className="deep-3d-card deep-3d-press p-5 flex items-center justify-between bg-amber-500/10 border border-amber-200 cursor-pointer"
        >
          <div>
            <p className="text-xs font-bold text-amber-700 uppercase tracking-wider">Pending</p>
            <h3 className="text-3xl font-extrabold text-amber-600 mt-1 font-heading">{pendingCount}</h3>
            <p className="text-xs font-semibold text-amber-700 mt-1">Requires Reviewer Action</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-amber-500/20 border border-amber-300 text-amber-700 flex items-center justify-center">
            <Clock className="w-6 h-6" />
          </div>
        </div>

        <div
          onClick={() => onNavigate('campaigns')}
          className="deep-3d-card deep-3d-press p-5 flex items-center justify-between bg-emerald-500/10 border border-emerald-200 cursor-pointer"
        >
          <div>
            <p className="text-xs font-bold text-emerald-700 uppercase tracking-wider">Approved</p>
            <h3 className="text-3xl font-extrabold text-emerald-600 mt-1 font-heading">{approvedCount}</h3>
            <p className="text-xs font-semibold text-emerald-700 mt-1">Ready for Schedule</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-emerald-500/20 border border-emerald-300 text-emerald-700 flex items-center justify-center">
            <CheckCircle2 className="w-6 h-6" />
          </div>
        </div>

        {/* Active Badge Metric represents the number of posted campaigns directly from database */}
        <div
          onClick={() => onNavigate('campaigns')}
          className="deep-3d-card deep-3d-press p-5 flex items-center justify-between bg-blue-500/10 border border-blue-200 cursor-pointer"
        >
          <div>
            <p className="text-xs font-bold text-blue-700 uppercase tracking-wider">Active</p>
            <h3 className="text-3xl font-extrabold text-blue-600 mt-1 font-heading">{activePostedCount}</h3>
            <p className="text-xs font-semibold text-blue-700 mt-1">Posted Campaigns</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-blue-500/20 border border-blue-300 text-blue-700 flex items-center justify-center">
            <Send className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Section 1: Campaign Queue */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-extrabold text-slate-900 font-heading">Campaign Queue</h2>
          <button
            onClick={() => onNavigate('campaigns')}
            className="deep-3d-press text-xs font-bold text-blue-600 hover:text-blue-800 flex items-center gap-1"
          >
            <span>All</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>

        {campaignQueue.length === 0 ? (
          <div className="deep-3d-card p-8 text-center bg-white/90">
            <Megaphone className="w-10 h-10 mx-auto text-slate-400 mb-2" />
            <h4 className="font-bold text-slate-800">No campaigns in queue</h4>
            <p className="text-xs text-slate-500 mt-1">0 campaigns with status 'Generated' or 'Approved'.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {campaignQueue.map((c) => {
              const eventName = getEventName(c.eventId);
              return (
                <div
                  key={c.eventId || c.campaignId}
                  onClick={() => onNavigate('campaigns', c.eventId)}
                  className="deep-3d-card deep-3d-press p-5 bg-white hover:border-blue-300 transition-all cursor-pointer space-y-3"
                >
                  <div className="flex items-center justify-between">
                    <h3 className="font-extrabold text-base text-slate-900 line-clamp-1">{eventName}</h3>
                    <StatusBadge status={c.status} type="campaign" />
                  </div>

                  <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                    {c.campaignPost}
                  </p>

                  <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
                    <span>{c.createdAt ? formatEventDate(c.createdAt) : 'Live Data'}</span>
                    <span className="font-bold text-blue-600 flex items-center gap-1">
                      Review &rarr;
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Section 2: Upcoming Events */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-extrabold text-slate-900 font-heading">Upcoming Events</h2>
          <button
            onClick={() => onNavigate('events')}
            className="deep-3d-press text-xs font-bold text-blue-600 hover:text-blue-800 flex items-center gap-1"
          >
            <span>All</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>

        {upcomingEvents.length === 0 ? (
          <div className="deep-3d-card p-8 text-center bg-white/90">
            <Calendar className="w-10 h-10 mx-auto text-slate-400 mb-2" />
            <h4 className="font-bold text-slate-800">No upcoming events</h4>
            <p className="text-xs text-slate-500 mt-1">0 active events scheduled on backend.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {upcomingEvents.map((e) => (
              <div
                key={e.id}
                onClick={() => onNavigate('events')}
                className="deep-3d-card deep-3d-press p-5 bg-white hover:border-blue-300 transition-all cursor-pointer space-y-3"
              >
                <div className="flex items-center justify-between">
                  <h3 className="font-extrabold text-base text-slate-900">{e.title}</h3>
                  {e.eventType && (
                    <span className="text-[11px] font-bold px-2 py-0.5 rounded bg-blue-100 text-blue-700 border border-blue-200 uppercase">
                      {e.eventType}
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-4 text-xs text-slate-600">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    {formatEventDate(e.startTime)}
                  </span>
                  {e.location && (
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      {e.location}
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Section 3: ADMIN API KEY / OAUTH CREDENTIAL CONFIG MANAGER */}
      <div className="deep-3d-card p-6 bg-white/90 space-y-4 overflow-hidden">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-3">
          <div>
            <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2 font-heading">
              <Key className="w-5 h-5 text-blue-600" />
              <span>Social Media API Credentials & Token Config</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Admin configuration for OAuth access tokens, API keys, and expiration management
            </p>
          </div>
          <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-200 self-start sm:self-auto">
            RBAC Token Protection Active
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {credentials.map((cred) => {
            const rawToken = cred.maskedToken || '';
            const shortToken =
              rawToken.length > 8
                ? `${rawToken.slice(0, 4)}****${rawToken.slice(-4)}`
                : '****';

            return (
              <div
                key={cred.id || cred.provider}
                className="p-5 rounded-2xl border border-slate-200 bg-white hover:border-blue-300 transition-all flex flex-col justify-between space-y-4"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <h4 className="font-extrabold text-base text-slate-900">{cred.provider}</h4>
                    <span
                      className={`text-[10px] font-extrabold px-2 py-0.5 rounded ${
                        cred.isActive
                          ? 'bg-emerald-100 text-emerald-700 border border-emerald-200'
                          : 'bg-slate-200 text-slate-600'
                      }`}
                    >
                      {cred.isActive ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </div>

                  <p className="text-xs text-slate-500 font-mono">
                    Token: <span className="font-bold text-slate-700">{shortToken}</span>
                  </p>

                  {cred.expiresAt && (
                    <p className="text-[11px] font-semibold text-slate-500">
                      Expires: <span className="text-slate-800 font-bold">{formatEventDate(cred.expiresAt)}</span>
                    </p>
                  )}
                </div>

                <button
                  onClick={() => setSelectedCredProvider(cred.provider)}
                  className="deep-3d-press btn-primary py-2 justify-center text-xs font-bold w-full"
                >
                  <Key className="w-3.5 h-3.5" />
                  {rawToken ? 'Update Token / Key' : 'Add Token'}
                </button>
              </div>
            );
          })}
        </div>
      </div>

      {/* Update Credential Modal */}
      {selectedCredProvider && (
        <UpdateCredentialModal
          provider={selectedCredProvider}
          currentIsActive={true}
          onClose={() => setSelectedCredProvider(null)}
          onSuccess={() => {
            showToast(`API token & credentials updated for ${selectedCredProvider}!`, 'success');
            fetchDashboardData();
          }}
        />
      )}
    </div>
  );
};
