import React, { useEffect, useState } from 'react';
import { Megaphone, Calendar, Clock, Key, ChevronRight, MapPin, AlertTriangle, RefreshCw, AlertCircle } from 'lucide-react';
import { Campaign, SpeehiveEvent, SocialMediaCredential } from '../types';
import { apiClient } from '../api/client';
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
  const [selectedCred, setSelectedCred] = useState<SocialMediaCredential | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isConnected, setIsConnected] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const defaultCredentials: SocialMediaCredential[] = [
    {
      id: 'cred-1',
      provider: 'LinkedIn',
      maskedToken: 'pk_live_9921827419',
      expiresAt: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(),
      isActive: true,
      updatedAt: '2026-07-20T10:00:00Z',
    },
    {
      id: 'cred-2',
      provider: 'Instagram',
      maskedToken: 'ig_live_4492810481',
      expiresAt: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString(),
      isActive: true,
      updatedAt: '2026-07-10T14:00:00Z',
    },
    {
      id: 'cred-3',
      provider: 'Teams',
      maskedToken: 'ms_live_8830192841',
      expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
      isActive: true,
      updatedAt: '2026-07-01T09:00:00Z',
    },
    {
      id: 'cred-4',
      provider: 'WhatsApp',
      maskedToken: 'wa_live_7719203841',
      expiresAt: new Date(Date.now() + 45 * 24 * 60 * 60 * 1000).toISOString(),
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
        const eventData: SpeehiveEvent[] = Array.isArray(eventRes.value.data) ? eventRes.value.data : [];
        setEvents(eventData);
        const titleMap = new Map<string, string>();
        eventData.forEach((e) => titleMap.set(e.id, e.title));
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
      console.error('Failed to fetch live dashboard data:', err);
      setErrorMessage('Operating with local fallback states due to API connection warning.');
      setCredentials(defaultCredentials);
      setIsConnected(false);
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
    if (!dateStr) return 'TBD';
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return dateStr;
      return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    } catch (e) {
      return dateStr;
    }
  };

  const getDaysUntilExpiration = (expiresAtStr?: string | null) => {
    if (!expiresAtStr) return null;
    try {
      const expires = new Date(expiresAtStr).getTime();
      if (isNaN(expires)) return null;
      const diff = expires - Date.now();
      return Math.ceil(diff / (1000 * 60 * 60 * 24));
    } catch (e) {
      return null;
    }
  };

  // Active Pending Campaign Queue (Generated or Approved)
  const campaignQueue = campaigns.filter((c) => {
    const status = (c.status || '').toLowerCase();
    return status === 'generated' || status === 'approved' || status === 'active';
  });

  // Count of Posted Campaigns/Events directly from database
  const postedEventsCount = campaigns.filter((c) => {
    const status = (c.status || '').toLowerCase();
    return status === 'published' || status === 'posted' || Boolean(c.postedAt);
  }).length;

  // Upcoming Active Events
  const upcomingEvents = events.filter((e) => {
    const status = (e.status || '').toLowerCase();
    return status !== 'cancelled';
  });

  // Credentials expiring within 7 days or expired
  const expiringCredentials = credentials.filter((c) => {
    if (!c.expiresAt) return false;
    const daysLeft = getDaysUntilExpiration(c.expiresAt);
    return daysLeft !== null && daysLeft <= 7;
  });

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

      {/* API CREDENTIALS EXPIRING SOON BANNER */}
      {expiringCredentials.length > 0 && (
        <div className="deep-3d-card p-5 bg-gradient-to-r from-amber-500/15 via-orange-500/10 to-amber-500/5 border border-amber-300 space-y-2">
          <div className="flex items-center gap-2 text-amber-700 font-extrabold text-xs uppercase tracking-wider">
            <AlertTriangle className="w-4 h-4 text-amber-600 animate-bounce" />
            <span>API Credentials Expiring Soon</span>
          </div>
          <div className="space-y-1 pl-6">
            {expiringCredentials.map((cred) => {
              const daysLeft = getDaysUntilExpiration(cred.expiresAt);
              return (
                <p key={cred.id || cred.provider} className="text-xs font-semibold text-slate-800">
                  • <strong className="text-amber-800">{cred.provider}</strong> API key{' '}
                  {daysLeft !== null && daysLeft <= 0 ? (
                    <span className="text-red-700 font-bold">has EXPIRED!</span>
                  ) : (
                    <span>
                      expires in <strong className="text-amber-900 font-mono font-bold">{daysLeft} days</strong> ({formatEventDate(cred.expiresAt || '')})
                    </span>
                  )}
                  {' '}— Please update token to maintain social media auto-posting!
                </p>
              );
            })}
          </div>
        </div>
      )}

      {/* Metric Quick Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-5 rounded-2xl bg-gradient-to-br from-blue-600 to-blue-700 text-white space-y-1 shadow-lg shadow-blue-500/25 border border-blue-500/80">
          <span className="text-xs font-bold uppercase tracking-wider text-blue-100 font-mono">Active Events</span>
          <div className="text-3xl font-extrabold font-heading text-white">{events.length}</div>
          <p className="text-[11px] text-blue-100 font-medium">Live in backend database</p>
        </div>

        <div className="p-5 rounded-2xl bg-gradient-to-br from-indigo-600 to-indigo-700 text-white space-y-1 shadow-lg shadow-indigo-500/25 border border-indigo-500/80">
          <span className="text-xs font-bold uppercase tracking-wider text-indigo-100 font-mono">Pending Approval</span>
          <div className="text-3xl font-extrabold font-heading text-white">{campaignQueue.length}</div>
          <p className="text-[11px] text-indigo-100 font-medium">Awaiting reviewer action</p>
        </div>

        <div className="p-5 rounded-2xl bg-gradient-to-br from-emerald-600 to-emerald-700 text-white space-y-1 shadow-lg shadow-emerald-500/25 border border-emerald-500/80">
          <span className="text-xs font-bold uppercase tracking-wider text-emerald-100 font-mono">Posted Events</span>
          <div className="text-3xl font-extrabold font-heading text-white">{postedEventsCount}</div>
          <p className="text-[11px] text-emerald-100 font-medium">Published to social media</p>
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
              <span>Social Media API Credentials & Token Expiration Config</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Admin configuration for OAuth access tokens, API keys, expiration dates, and active status
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {credentials.map((cred) => {
            const rawToken = cred.maskedToken || '';
            const shortToken =
              rawToken.length > 8
                ? `${rawToken.slice(0, 4)}****${rawToken.slice(-4)}`
                : '****';

            const daysLeft = getDaysUntilExpiration(cred.expiresAt);
            const isExpired = daysLeft !== null && daysLeft <= 0;
            const isExpiringSoon = daysLeft !== null && daysLeft > 0 && daysLeft <= 7;

            return (
              <div
                key={cred.id || cred.provider}
                className={`p-5 rounded-2xl border transition-all flex flex-col justify-between space-y-4 bg-white ${
                  isExpired
                    ? 'border-red-300 bg-red-50/30'
                    : isExpiringSoon
                    ? 'border-amber-300 bg-amber-50/30'
                    : 'border-slate-200 hover:border-blue-300'
                }`}
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <h4 className="font-extrabold text-base text-slate-900">{cred.provider}</h4>
                    <div className="flex items-center gap-1.5">
                      {isExpired ? (
                        <span className="text-[10px] font-extrabold px-2 py-0.5 rounded bg-red-100 text-red-700 border border-red-300 animate-pulse uppercase">
                          EXPIRED
                        </span>
                      ) : isExpiringSoon ? (
                        <span className="text-[10px] font-extrabold px-2 py-0.5 rounded bg-amber-100 text-amber-800 border border-amber-300 uppercase">
                          EXPIRING SOON
                        </span>
                      ) : null}
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
                  </div>

                  <p className="text-xs text-slate-500 font-mono">
                    Token: <span className="font-bold text-slate-700">{shortToken}</span>
                  </p>

                  {cred.expiresAt && (
                    <div className="text-[11px] font-semibold text-slate-600 space-y-0.5">
                      <p>
                        Expires:{' '}
                        <span className="text-slate-800 font-bold font-mono">
                          {formatEventDate(cred.expiresAt)}
                        </span>
                      </p>
                      {daysLeft !== null && (
                        <p className={`font-mono text-[10px] font-bold ${
                          isExpired ? 'text-red-600' : isExpiringSoon ? 'text-amber-700' : 'text-slate-400'
                        }`}>
                          {isExpired
                            ? '⚠️ Key expired! Please update token.'
                            : `⏰ ${daysLeft} ${daysLeft === 1 ? 'day' : 'days'} remaining`}
                        </p>
                      )}
                    </div>
                  )}
                </div>

                <button
                  onClick={() => setSelectedCred(cred)}
                  className="deep-3d-press btn-primary py-2 justify-center text-xs font-bold w-full"
                >
                  <Key className="w-3.5 h-3.5" />
                  {rawToken ? 'Update Token & Expiration' : 'Add Token'}
                </button>
              </div>
            );
          })}
        </div>
      </div>

      {/* Update Credential Modal */}
      {selectedCred && (
        <UpdateCredentialModal
          providerName={selectedCred.provider}
          currentIsActive={selectedCred.isActive}
          currentExpiresAt={selectedCred.expiresAt}
          onClose={() => setSelectedCred(null)}
          onSuccess={() => {
            showToast(`API token & credentials updated for ${selectedCred.provider}!`, 'success');
            fetchDashboardData();
          }}
        />
      )}
    </div>
  );
};
