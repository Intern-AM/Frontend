import React, { useState, useEffect } from 'react';
import { Search, Filter, Megaphone, CheckCircle2 } from 'lucide-react';
import { Campaign, SpeehiveEvent } from '../types';
import { apiClient, getFormattedImageUrl } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { ViewModeSwitcher, ViewMode } from '../components/ViewModeSwitcher';
import { useAuth } from '../context/AuthContext';

interface CampaignsProps {
  onSelectCampaign: (eventId: string) => void;
}

export const Campaigns: React.FC<CampaignsProps> = ({ onSelectCampaign }) => {
  const { role } = useAuth();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [eventTitleMap, setEventTitleMap] = useState<Map<string, string>>(new Map());
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchCampaignsAndEvents = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const [campaignRes, eventRes] = await Promise.allSettled([
        apiClient.get('/api/Campaigns'),
        apiClient.get('/api/Events'),
      ]);

      if (campaignRes.status === 'fulfilled') {
        const campaignData: Campaign[] = Array.isArray(campaignRes.value.data) ? campaignRes.value.data : [];
        setCampaigns(campaignData);
      } else {
        setCampaigns([]);
      }

      if (eventRes.status === 'fulfilled') {
        const eventData: SpeehiveEvent[] = Array.isArray(eventRes.value.data) ? eventRes.value.data : [];
        const titleMap = new Map<string, string>();
        eventData.forEach((e) => titleMap.set(e.id, e.title));
        setEventTitleMap(titleMap);
      }
    } catch (err: any) {
      console.error('API call failed:', err);
      setErrorMessage('Failed to fetch campaigns from backend server.');
      setCampaigns([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCampaignsAndEvents();
  }, []);

  const handleQuickApprove = async (e: React.MouseEvent, eventId: string) => {
    e.stopPropagation();
    try {
      await apiClient.post('/api/Approval/approve', { eventId, comments: 'Approved' });
      fetchCampaignsAndEvents();
    } catch (err: any) {
      console.error('Approve failed:', err);
    }
  };

  const getEventName = (eventId: string) => {
    return eventTitleMap.get(eventId) || `Event: ${eventId}`;
  };

  const filteredCampaigns = campaigns.filter((c) => {
    const post = c.campaignPost || '';
    const tags = c.hashtags || '';
    const evtId = c.eventId || '';
    const evtName = getEventName(evtId);

    const matchesSearch =
      post.toLowerCase().includes(searchQuery.toLowerCase()) ||
      tags.toLowerCase().includes(searchQuery.toLowerCase()) ||
      evtId.toLowerCase().includes(searchQuery.toLowerCase()) ||
      evtName.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatus =
      selectedStatus === 'ALL' || (c.status && c.status.toUpperCase() === selectedStatus.toUpperCase());

    return matchesSearch && matchesStatus;
  });

  // Parity with CampaignListScreen.kt (ALL, GENERATED, APPROVED, REJECTED)
  const statuses = ['ALL', 'GENERATED', 'APPROVED', 'REJECTED'];

  return (
    <div className="space-y-6 pb-12">
      {/* Header Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-3 font-heading">
            <Megaphone className="w-8 h-8 text-blue-600" />
            <span>Campaign Management</span>
          </h1>
          <p className="text-sm font-medium text-slate-500 mt-1">
            Review campaign copy, hashtags, AI image prompts, and per-platform schedules
          </p>
        </div>

        <ViewModeSwitcher currentMode={viewMode} onModeChange={setViewMode} />
      </div>

      {/* Filter Bar */}
      <div className="deep-3d-card p-4 bg-white/90 space-y-3">
        <div className="relative">
          <Search className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search campaigns by event name, copy, or hashtags..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input-field pl-11"
          />
        </div>

        <div className="flex items-center gap-1.5 overflow-x-auto pt-1 pb-1">
          <span className="text-xs font-bold text-slate-500 mr-2 flex items-center gap-1">
            <Filter className="w-3.5 h-3.5" /> Status:
          </span>
          {statuses.map((s) => (
            <button
              key={s}
              onClick={() => setSelectedStatus(s)}
              className={`deep-3d-press px-3 py-1 rounded-xl text-xs font-bold transition-all ${
                selectedStatus === s
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-500/30'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 text-xs font-bold border border-red-200">
          {errorMessage}
        </div>
      )}

      {/* Campaign List / Grid */}
      {isLoading ? (
        <div className="p-12 text-center">
          <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-sm font-semibold text-slate-600">Loading live campaigns from backend...</p>
        </div>
      ) : filteredCampaigns.length === 0 ? (
        <div className="deep-3d-card p-12 text-center bg-white/90">
          <Megaphone className="w-12 h-12 text-slate-400 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-800">No campaigns found on server</h3>
          <p className="text-xs text-slate-500 mt-1">There are currently 0 campaigns returned by GET /api/Campaigns.</p>
        </div>
      ) : viewMode === 'grid' ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredCampaigns.map((c) => {
            const formattedImageUrl = getFormattedImageUrl(c.imageUrl);
            const eventName = getEventName(c.eventId);

            return (
              <div
                key={c.eventId || c.campaignId}
                onClick={() => onSelectCampaign(c.eventId)}
                className="deep-3d-card deep-3d-press overflow-hidden flex flex-col bg-white hover:border-blue-300 transition-all cursor-pointer"
              >
                {formattedImageUrl ? (
                  <div className="relative h-44 w-full bg-slate-900 overflow-hidden">
                    <img
                      src={formattedImageUrl}
                      alt={eventName}
                      className="w-full h-full object-cover opacity-90 hover:opacity-100 transition-opacity"
                    />
                    <div className="absolute top-3 right-3">
                      <StatusBadge status={c.status} type="campaign" />
                    </div>
                  </div>
                ) : (
                  <div className="h-28 w-full bg-gradient-to-r from-blue-600 to-indigo-700 p-4 flex items-center justify-between">
                    <h4 className="font-bold text-white text-base leading-snug">{eventName}</h4>
                    <StatusBadge status={c.status} type="campaign" />
                  </div>
                )}

                <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                  <div>
                    <h3 className="font-extrabold text-base text-slate-900 leading-snug">{eventName}</h3>
                    <p className="text-xs text-slate-600 mt-1.5 line-clamp-2 leading-relaxed">
                      {c.campaignPost}
                    </p>

                    {c.hashtags && (
                      <div className="mt-3">
                        <span className="text-[11px] font-bold font-mono text-blue-700 bg-blue-50 px-2 py-0.5 rounded-md border border-blue-200">
                          {c.hashtags}
                        </span>
                      </div>
                    )}
                  </div>

                  <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                    <span className="text-[11px] font-semibold text-slate-400">
                      {c.createdAt ? new Date(c.createdAt).toLocaleDateString() : 'Live Data'}
                    </span>

                    {role === 'Reviewer' && c.status && c.status.toLowerCase() === 'generated' && (
                      <button
                        onClick={(e) => handleQuickApprove(e, c.eventId)}
                        className="deep-3d-press px-3 py-1 rounded-lg bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 flex items-center gap-1 shadow-sm"
                      >
                        <CheckCircle2 className="w-3.5 h-3.5" /> Approve
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="deep-3d-card overflow-hidden bg-white/90 divide-y divide-slate-200">
          {filteredCampaigns.map((c) => {
            const eventName = getEventName(c.eventId);
            return (
              <div
                key={c.eventId || c.campaignId}
                onClick={() => onSelectCampaign(c.eventId)}
                className="deep-3d-press p-4 hover:bg-blue-50/50 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4"
              >
                <div className="flex items-center gap-4 flex-1">
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-extrabold text-slate-900 text-base">{eventName}</h3>
                      <StatusBadge status={c.status} type="campaign" />
                    </div>
                    <p className="text-xs text-slate-600 mt-1 line-clamp-1">{c.campaignPost}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span className="text-xs font-bold text-blue-600 bg-blue-50 px-3 py-1.5 rounded-xl border border-blue-200">
                    Open Campaign &rarr;
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
