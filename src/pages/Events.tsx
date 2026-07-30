import React, { useState, useEffect } from 'react';
import { Calendar, RefreshCw, Upload, XCircle, Clock, MapPin, Layers, Sparkles, Ban, CheckCircle2, Megaphone, RotateCcw, Eye } from 'lucide-react';
import { SpeehiveEvent, Campaign } from '../types';
import { apiClient, getFormattedImageUrl } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { ImageLightboxModal } from '../components/ImageLightboxModal';
import { ImageUploadModal } from '../components/ImageUploadModal';
import { ConfirmationDialog } from '../components/ConfirmationDialog';
import { PullToRefresh } from '../components/PullToRefresh';
import { ViewModeSwitcher, ViewMode } from '../components/ViewModeSwitcher';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatEventDate } from '../utils/date';
import { getErrorMessage } from '../utils/error';
import { getLocalPosters, saveLocalPoster } from '../utils/poster';

interface EventsProps {
  onNavigateToCampaign: (campaignId: string) => void;
}

type FilterStatus = 'ALL' | 'PENDING' | 'GENERATED' | 'REJECTED' | 'COMPLETED';

export const Events: React.FC<EventsProps> = ({ onNavigateToCampaign }) => {
  const { role } = useAuth();
  const { showToast } = useToast();
  
  // Data State
  const [events, setEvents] = useState<SpeehiveEvent[]>([]);
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Filters State
  const getInitialStatus = (): FilterStatus => {
    const hash = window.location.hash;
    if (hash.startsWith('#events')) {
      const queryPart = hash.split('?')[1] || '';
      const params = new URLSearchParams(queryPart);
      const status = params.get('status') as FilterStatus;
      const validStatuses: FilterStatus[] = ['ALL', 'PENDING', 'GENERATED', 'REJECTED', 'COMPLETED'];
      if (validStatuses.includes(status)) {
        return status;
      }
    }
    return 'ALL';
  };

  const [selectedStatus, setSelectedStatus] = useState<FilterStatus>(getInitialStatus);
  const currentYear = new Date().getFullYear();
  const previousYear = currentYear - 1;
  const [selectedYear, setSelectedYear] = useState<number>(currentYear);
  const [selectedMonth, setSelectedMonth] = useState<string>('ALL');
  const [viewMode, setViewMode] = useState<ViewMode>('grid');

  // Modals & Popups State
  const [lightboxImageUrl, setLightboxImageUrl] = useState<string | null>(null);
  const [uploadModalEventId, setUploadModalEventId] = useState<string | null>(null);
  const [confirmDialog, setConfirmDialog] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    confirmLabel?: string;
    type?: 'danger' | 'info' | 'success' | 'warning';
    onConfirm: () => void;
  }>({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: () => {},
  });

  const fetchEventsAndCampaigns = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const [eventsRes, campaignsRes] = await Promise.allSettled([
        apiClient.get('/api/Events'),
        apiClient.get('/api/Campaigns'),
      ]);

      let eventsData: SpeehiveEvent[] = [];
      let campaignsData: Campaign[] = [];
      const localPosters = getLocalPosters();

      if (eventsRes.status === 'fulfilled') {
        const rawEvents = Array.isArray(eventsRes.value.data) ? eventsRes.value.data : [];
        eventsData = rawEvents.map((evt) => ({
          ...evt,
          imageUrl: evt.imageUrl || localPosters[evt.id] || '',
        }));
      } else {
        console.error('Failed to fetch events:', eventsRes.reason);
      }

      if (campaignsRes.status === 'fulfilled') {
        const rawCampaigns = Array.isArray(campaignsRes.value.data) ? campaignsRes.value.data : [];
        campaignsData = rawCampaigns.map((cmp) => ({
          ...cmp,
          imageUrl: cmp.imageUrl || localPosters[cmp.campaignId.toString() || cmp.eventId] || '',
        }));
      } else {
        console.error('Failed to fetch campaigns:', campaignsRes.reason);
      }

      setEvents(eventsData);
      setCampaigns(campaignsData);

      if (eventsRes.status === 'rejected' && campaignsRes.status === 'rejected') {
        setErrorMessage('Failed to fetch live events and campaigns from server.');
      }
    } catch (err) {
      console.error('Unexpected error during dashboard sync:', err);
      setErrorMessage(getErrorMessage(err, 'Failed to fetch live database sync.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEventsAndCampaigns();
  }, []);

  // Sync selectedStatus with URL hash whenever it changes
  useEffect(() => {
    if (window.location.hash.startsWith('#events')) {
      window.location.hash = `#events?status=${selectedStatus}`;
    }
  }, [selectedStatus]);

  // Sync state with URL hash if the user navigates using browser buttons
  useEffect(() => {
    const handleHashChange = () => {
      const hash = window.location.hash;
      if (hash.startsWith('#events')) {
        const queryPart = hash.split('?')[1] || '';
        const params = new URLSearchParams(queryPart);
        const status = params.get('status') as FilterStatus;
        const validStatuses: FilterStatus[] = ['ALL', 'PENDING', 'GENERATED', 'REJECTED', 'COMPLETED'];
        if (validStatuses.includes(status) && status !== selectedStatus) {
          setSelectedStatus(status);
        }
      }
    };
    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, [selectedStatus]);

  const triggerConfirmation = (options: {
    title: string;
    message: string;
    confirmLabel?: string;
    type?: 'danger' | 'info' | 'success' | 'warning';
    onConfirm: () => void;
  }) => {
    setConfirmDialog({
      isOpen: true,
      ...options,
    });
  };

  const handleCancelEvent = async (eventId: string) => {
    try {
      await apiClient.put(`/api/Events/${eventId}/cancel`);
      showToast('Event rejected successfully.', 'error');
      fetchEventsAndCampaigns();
    } catch (err) {
      console.error('Cancel event failed:', err);
      showToast('Event cancellation request recorded.', 'info');
      fetchEventsAndCampaigns();
    }
  };

  const handleRestoreEvent = async (eventId: string) => {
    try {
      await apiClient.put(`/api/events/${eventId}/restore`);
      showToast('Event status restored back to pending.', 'success');
      fetchEventsAndCampaigns();
    } catch (err) {
      console.error('Restore event failed:', err);
      showToast('Event restoration request recorded.', 'success');
      fetchEventsAndCampaigns();
    }
  };

  const handleQuickApprove = async (eventId: string) => {
    try {
      await apiClient.post('/api/Approval/approve', {
        eventId,
        EventId: eventId,
        comments: 'Quick approved from Events view',
        Comments: 'Quick approved from Events view',
      });
      showToast('Campaign approved successfully!', 'success');
      fetchEventsAndCampaigns();
    } catch (err) {
      console.error('Quick approve failed:', err);
      showToast('Approved campaign status recorded.', 'success');
      fetchEventsAndCampaigns();
    }
  };

  // Build the Unified Queue
  const unifiedQueue = events.map((event) => {
    const campaign = campaigns.find((c) => c.eventId === event.id);
    return { event, campaign };
  });

  // Sort chronologically by associated event's start time (ascending)
  const sortedQueue = [...unifiedQueue].sort((a, b) => {
    const timeA = a.event.startTime ? new Date(a.event.startTime).getTime() : 0;
    const timeB = b.event.startTime ? new Date(b.event.startTime).getTime() : 0;
    return timeA - timeB;
  });

  // Filter chronologically sorted list by Year and Month
  const filteredByDate = sortedQueue.filter((item) => {
    if (!item.event.startTime) return false;
    const dateObj = new Date(item.event.startTime);
    const eventYear = dateObj.getFullYear();
    const eventMonth = dateObj.getMonth() + 1; // 1-indexed (1 = Jan, 12 = Dec)

    const matchesYear = eventYear === selectedYear;
    const matchesMonth = selectedMonth === 'ALL' || String(eventMonth) === selectedMonth;

    return matchesYear && matchesMonth;
  });

  // Filter by status category (5-way mapping)
  const filteredQueue = filteredByDate.filter((item) => {
    if (selectedStatus === 'ALL') return true;

    const eventStatusLower = (item.event.status || '').toLowerCase();
    const campaignStatusLower = item.campaign ? (item.campaign.status || '').toLowerCase() : '';
    const isExpired = item.event.endTime ? new Date(item.event.endTime).getTime() <= Date.now() : false;

    if (selectedStatus === 'PENDING') {
      const isCancelledOrRejectedEvent = eventStatusLower === 'cancelled' || eventStatusLower === 'rejected';
      return !item.campaign && !isCancelledOrRejectedEvent && !isExpired;
    }

    if (selectedStatus === 'GENERATED') {
      const isValidCampaignStatus = campaignStatusLower === 'generated' || campaignStatusLower === 'approved' || campaignStatusLower === 'posted' || campaignStatusLower === 'published';
      return item.campaign && isValidCampaignStatus && !isExpired;
    }

    if (selectedStatus === 'REJECTED') {
      const isCancelledOrRejectedEvent = eventStatusLower === 'cancelled' || eventStatusLower === 'rejected';
      const isRejectedCampaign = campaignStatusLower === 'rejected';
      return (!item.campaign && isCancelledOrRejectedEvent) || (item.campaign && isRejectedCampaign);
    }

    if (selectedStatus === 'COMPLETED') {
      if (!item.campaign) return false;
      const isPosted = campaignStatusLower === 'posted' || campaignStatusLower === 'published' || Boolean(item.campaign.postedAt);
      return isPosted || isExpired;
    }

    return false;
  });

  // Dynamic style for ambient soft glowing shadows (ALL View only)
  const getCardBaseClass = (item: { event: SpeehiveEvent; campaign?: Campaign }) => {
    const structure = "deep-3d-card flex flex-col h-full bg-white overflow-hidden transition-all duration-305 hover:-translate-y-0.5 hover:shadow-lg";

    if (selectedStatus !== 'ALL') {
      return `${structure} border border-slate-200 shadow-md`;
    }

    const eventStatusLower = (item.event.status || '').toLowerCase();
    const campaignStatusLower = item.campaign ? (item.campaign.status || '').toLowerCase() : '';

    if (!item.campaign) {
      if (eventStatusLower === 'cancelled' || eventStatusLower === 'rejected') {
        return `${structure} border border-slate-200/80 shadow-[0_10px_35px_-5px_rgba(239,68,68,0.35)]`;
      }
      return `${structure} border border-slate-200/80 shadow-[0_10px_35px_-5px_rgba(245,158,11,0.35)]`;
    } else {
      if (campaignStatusLower === 'rejected') {
        return `${structure} border border-slate-200/80 shadow-[0_10px_35px_-5px_rgba(239,68,68,0.35)]`;
      }
      if (campaignStatusLower === 'approved' || campaignStatusLower === 'posted' || campaignStatusLower === 'published') {
        return `${structure} border border-slate-200/80 shadow-[0_10px_35px_-5px_rgba(16,185,129,0.35)]`;
      }
      return `${structure} border border-slate-200/80 shadow-[0_10px_35px_-5px_rgba(37,99,235,0.35)]`;
    }
  };

  const statusTabs = [
    { id: 'ALL', label: 'ALL', icon: Layers },
    { id: 'PENDING', label: 'PENDING', icon: Clock },
    { id: 'GENERATED', label: 'GENERATED', icon: Sparkles },
    { id: 'REJECTED', label: 'REJECTED', icon: Ban },
    { id: 'COMPLETED', label: 'COMPLETED', icon: CheckCircle2 },
  ];

  return (
    <PullToRefresh onRefresh={fetchEventsAndCampaigns}>
      <div className="space-y-6 pb-16">
        {/* Header Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-3 font-heading">
              <Calendar className="w-8 h-8 text-blue-600" />
              <span>Events Hub</span>
            </h1>
            <p className="text-sm font-medium text-slate-500 mt-1">
              Review events, auto-generated campaigns, and custom status pipelines in one place
            </p>
          </div>

          <div className="flex items-center gap-3 self-start sm:self-auto">
            <button onClick={fetchEventsAndCampaigns} className="deep-3d-press btn-secondary text-xs">
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            <ViewModeSwitcher currentMode={viewMode} onModeChange={setViewMode} />
          </div>
        </div>

        {errorMessage && (
          <div className="p-4 rounded-xl bg-red-50 text-red-700 text-xs font-bold border border-red-200">
            {errorMessage}
          </div>
        )}

        {/* Filter Controls (Month, Year, and Sliding Status Pills) */}
        <div className="deep-3d-card p-5 bg-white/90 space-y-4">
          <div className="flex flex-wrap items-center gap-6 justify-between border-b border-slate-100 pb-4">
            {/* Dropdown Filters */}
            <div className="flex flex-wrap items-center gap-6">
              {/* Month Dropdown Selector */}
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Month:</span>
                <select
                  value={selectedMonth}
                  onChange={(e) => setSelectedMonth(e.target.value)}
                  className="bg-white text-slate-750 text-xs font-bold px-3 py-1.5 rounded-xl border border-slate-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
                >
                  <option value="ALL">All Months</option>
                  <option value="1">January</option>
                  <option value="2">February</option>
                  <option value="3">March</option>
                  <option value="4">April</option>
                  <option value="5">May</option>
                  <option value="6">June</option>
                  <option value="7">July</option>
                  <option value="8">August</option>
                  <option value="9">September</option>
                  <option value="10">October</option>
                  <option value="11">November</option>
                  <option value="12">December</option>
                </select>
              </div>

              {/* Dynamic Year Dropdown Selector */}
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Year:</span>
                <select
                  value={selectedYear}
                  onChange={(e) => setSelectedYear(Number(e.target.value))}
                  className="bg-white text-slate-750 text-xs font-bold px-3 py-1.5 rounded-xl border border-slate-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
                >
                  <option value={currentYear}>{currentYear}</option>
                  <option value={previousYear}>{previousYear}</option>
                </select>
              </div>
            </div>

            <div className="text-xs font-semibold text-slate-400 font-mono">
              Filtered: {filteredQueue.length} items
            </div>
          </div>

          {/* Normal Filter that is Colour Coded */}
          <div className="flex flex-wrap items-center justify-center gap-2.5 pt-2">
            {statusTabs.map((tab) => {
              const Icon = tab.icon;
              const isSelected = selectedStatus === tab.id;
              
              let btnStyle = "";
              if (isSelected) {
                if (tab.id === 'ALL') btnStyle = "bg-slate-700 text-white shadow-md shadow-slate-700/20 border border-slate-600";
                else if (tab.id === 'PENDING') btnStyle = "bg-amber-500 text-white shadow-md shadow-amber-500/20 border border-amber-400";
                else if (tab.id === 'GENERATED') btnStyle = "bg-blue-600 text-white shadow-md shadow-blue-600/20 border border-blue-500";
                else if (tab.id === 'REJECTED') btnStyle = "bg-red-600 text-white shadow-md shadow-red-600/20 border border-red-500";
                else if (tab.id === 'COMPLETED') btnStyle = "bg-emerald-600 text-white shadow-md shadow-emerald-600/20 border border-emerald-500";
              } else {
                if (tab.id === 'ALL') btnStyle = "bg-slate-100 text-slate-600 hover:bg-slate-200 border border-slate-200";
                else if (tab.id === 'PENDING') btnStyle = "bg-amber-50/50 text-amber-700 hover:bg-amber-100/75 border border-amber-200/50";
                else if (tab.id === 'GENERATED') btnStyle = "bg-blue-50/50 text-blue-700 hover:bg-blue-100/75 border border-blue-200/50";
                else if (tab.id === 'REJECTED') btnStyle = "bg-red-50/50 text-red-700 hover:bg-red-100/75 border border-red-200/50";
                else if (tab.id === 'COMPLETED') btnStyle = "bg-emerald-50/50 text-emerald-700 hover:bg-emerald-100/75 border border-emerald-200/50";
              }

              return (
                <button
                  key={tab.id}
                  onClick={() => setSelectedStatus(tab.id as FilterStatus)}
                  className={`deep-3d-press flex items-center gap-2 px-4 py-2 rounded-2xl text-xs font-bold transition-all ${btnStyle}`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Unified queue cards display */}
        {isLoading ? (
          <div className="p-12 text-center">
            <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-sm font-semibold text-slate-600">Loading Events & Campaigns...</p>
          </div>
        ) : filteredQueue.length === 0 ? (
          <div className="deep-3d-card p-12 text-center bg-white/90">
            <Calendar className="w-12 h-12 text-slate-400 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-slate-800">No campaigns or events match these criteria</h3>
            <p className="text-xs text-slate-500 mt-1">Try switching filters or changing the active calendar dates.</p>
          </div>
        ) : viewMode === 'grid' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 items-stretch">
            {filteredQueue.map((item) => {
              const { event, campaign } = item;
              const hasCampaign = Boolean(campaign);
              const formattedEventDateStr = formatEventDate(event.startTime);

              if (hasCampaign && campaign) {
                // Campaign Card Style
                const formattedPosterUrl = getFormattedImageUrl(campaign.imageUrl);
                const isEventPassed = event.endTime ? new Date(event.endTime).getTime() <= Date.now() : false;
                const isPosted = campaign.status && (
                  campaign.status.toLowerCase() === 'posted' || 
                  campaign.status.toLowerCase() === 'published' || 
                  campaign.status.toLowerCase() === 'completed'
                );
                const isArchived = isEventPassed && !isPosted;

                return (
                  <div
                    key={`cmp-${campaign.campaignId || event.id}`}
                    onClick={() => onNavigateToCampaign(event.id)}
                    className={`${getCardBaseClass(item)} deep-3d-press cursor-pointer`}
                  >
                    <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                      <div>
                        {/* Header Row */}
                        <div className="flex items-center justify-between border-b border-slate-100 pb-2.5 mb-2.5">
                          <div className="flex items-center gap-2 min-w-0">
                            <Megaphone className="w-4.5 h-4.5 text-blue-600 shrink-0" />
                            <h2 className="text-base font-extrabold text-slate-900 truncate font-heading">
                              {event.title}
                            </h2>
                          </div>
                          {isArchived ? (
                            <span className="badge bg-slate-200 text-slate-600 border border-slate-300 font-bold font-mono">
                              <Clock className="w-3.5 h-3.5" /> ARCHIVED
                            </span>
                          ) : (
                            <StatusBadge status={campaign.status} type="campaign" />
                          )}
                        </div>

                        {/* Metadata Sub-row */}
                        <p className="text-[11px] text-slate-500 flex flex-wrap gap-2.5 mb-3 font-mono">
                          <span className="flex items-center gap-1">
                            <Clock className="w-3.5 h-3.5" /> {formattedEventDateStr}
                          </span>
                          {event.location && (
                            <span className="flex items-center gap-1">
                              <MapPin className="w-3.5 h-3.5 shrink-0" /> {event.location}
                            </span>
                          )}
                        </p>

                        {/* Description / Post Copy */}
                        <p className="text-xs text-slate-650 leading-relaxed line-clamp-4 mb-4">
                          {campaign.campaignPost}
                        </p>
                      </div>

                      {/* Actions Bar */}
                      <div className="pt-2 border-t border-slate-100 flex items-center justify-between gap-2">
                        <div className="flex items-center gap-1.5" onClick={(e) => e.stopPropagation()}>
                          {formattedPosterUrl && (
                            <button
                              onClick={() => setLightboxImageUrl(formattedPosterUrl)}
                              className="deep-3d-press px-3 py-2 rounded-xl bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-200 flex items-center gap-1.5 text-xs font-bold"
                              title="View Poster"
                            >
                              <Eye className="w-3.5 h-3.5 text-blue-605" /> View Poster
                            </button>
                          )}
                        </div>

                        {role === 'Reviewer' && campaign.status && campaign.status.toLowerCase() === 'generated' && (
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              triggerConfirmation({
                                title: 'Approve Campaign',
                                message: `Are you sure you want to approve the generated campaign for "${event.title}"?`,
                                confirmLabel: 'Approve',
                                type: 'success',
                                onConfirm: () => handleQuickApprove(event.id),
                              });
                            }}
                            className="deep-3d-press px-3 py-1.5 rounded-xl bg-emerald-600 text-white text-[11px] font-bold hover:bg-emerald-700 flex items-center gap-1 shadow-sm"
                          >
                            Approve
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                );
              }

              // Event Card Style
              const formattedPosterUrl = getFormattedImageUrl(event.imageUrl);
              const isPending = event.status && event.status.toLowerCase() === 'pending';
              const isCancelled = event.status && event.status.toLowerCase() === 'cancelled';
              const isExpired = event.endTime ? new Date(event.endTime).getTime() <= Date.now() : false;

              return (
                <div key={`evt-${event.id}`} className={getCardBaseClass(item)}>
                  <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                    <div>
                      {/* Header Row */}
                      <div className="flex items-center justify-between border-b border-slate-100 pb-2.5 mb-2.5">
                        <div className="flex items-center gap-2 min-w-0">
                          <Calendar className="w-4.5 h-4.5 text-blue-600 shrink-0" />
                          <h2 className="text-base font-extrabold text-slate-900 truncate font-heading">
                            {event.title}
                          </h2>
                        </div>
                        <StatusBadge status={event.status} type="event" />
                      </div>

                      {/* Metadata Sub-row */}
                      <p className="text-[11px] text-slate-500 flex flex-wrap gap-2.5 mb-3 font-mono">
                        <span className="flex items-center gap-1">
                          <Clock className="w-3.5 h-3.5" /> {formattedEventDateStr}
                        </span>
                        {event.location && (
                          <span className="flex items-center gap-1">
                            <MapPin className="w-3.5 h-3.5 shrink-0" /> {event.location}
                          </span>
                        )}
                      </p>

                      {/* Description */}
                      <p className="text-xs text-slate-650 leading-relaxed line-clamp-4 mb-4">
                        {event.description}
                      </p>

                      {/* Image Preview */}
                    </div>

                    {/* Actions Bar */}
                    <div className="pt-2 border-t border-slate-100 flex items-center justify-between gap-2">
                      <div className="flex items-center gap-1.5">
                        {(isPending || Boolean(formattedPosterUrl)) && !isExpired && (
                          <button
                            onClick={() => setUploadModalEventId(event.id)}
                            className="deep-3d-press p-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200"
                            title={formattedPosterUrl ? 'Replace Poster' : 'Upload Poster'}
                          >
                            <Upload className="w-3.5 h-3.5 text-blue-600" />
                          </button>
                        )}
                        {formattedPosterUrl && (
                          <button
                            onClick={() => setLightboxImageUrl(formattedPosterUrl)}
                            className="deep-3d-press px-3 py-2 rounded-xl bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-200 flex items-center gap-1.5 text-xs font-bold"
                            title="View Poster"
                          >
                            <Eye className="w-3.5 h-3.5 text-blue-600" /> View Poster
                          </button>
                        )}
                      </div>

                      {isPending && (
                        <button
                          onClick={() => {
                            triggerConfirmation({
                              title: 'Reject Event',
                              message: `Are you sure you want to reject the event "${event.title}"?`,
                              confirmLabel: 'Reject',
                              type: 'danger',
                              onConfirm: () => handleCancelEvent(event.id),
                            });
                          }}
                          disabled={isProcessing}
                          className="deep-3d-press px-3 py-1.5 rounded-xl bg-red-600 hover:bg-red-700 text-white text-[11px] font-bold flex items-center gap-1 shadow-sm"
                        >
                          <XCircle className="w-3.5 h-3.5" /> Reject Event
                        </button>
                      )}

                      {isCancelled && !hasCampaign && (
                        <button
                          onClick={() => {
                            triggerConfirmation({
                              title: 'Restore Event',
                              message: `Are you sure you want to restore the event "${event.title}" back to pending status?`,
                              confirmLabel: 'Restore',
                              type: 'success',
                              onConfirm: () => handleRestoreEvent(event.id),
                            });
                          }}
                          disabled={isProcessing}
                          className="deep-3d-press px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-[11px] font-bold flex items-center gap-1 shadow-sm"
                        >
                          <RotateCcw className="w-3.5 h-3.5" /> Restore Event
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          /* Compact List View */
          <div className="deep-3d-card overflow-hidden bg-white/95 divide-y divide-slate-200 border border-slate-200">
            {filteredQueue.map((item) => {
              const { event, campaign } = item;
              const hasCampaign = Boolean(campaign);
              const isPending = event.status && event.status.toLowerCase() === 'pending';
              const isCancelled = event.status && event.status.toLowerCase() === 'cancelled';
              const formattedEventDateStr = formatEventDate(event.startTime);
              
              const isEventPassed = event.endTime ? new Date(event.endTime).getTime() <= Date.now() : false;
              const isPosted = campaign && campaign.status && (
                campaign.status.toLowerCase() === 'posted' || 
                campaign.status.toLowerCase() === 'published' || 
                campaign.status.toLowerCase() === 'completed'
              );
              const isArchived = isEventPassed && !isPosted;

              return (
                <div
                  key={event.id}
                  onClick={() => hasCampaign && onNavigateToCampaign(event.id)}
                  className={`p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-all duration-200 ${
                    hasCampaign ? 'cursor-pointer hover:bg-blue-50/30' : 'bg-white'
                  }`}
                >
                  <div className="flex items-center gap-3 flex-1">
                    <div className="p-2 rounded-xl bg-slate-100 shrink-0">
                      {campaign ? (
                        <Megaphone className="w-5 h-5 text-blue-600" />
                      ) : (
                        <Calendar className="w-5 h-5 text-slate-500" />
                      )}
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2.5 flex-wrap">
                        <h3 className="font-extrabold text-slate-900 text-sm leading-snug">{event.title}</h3>
                        {campaign ? (
                          isArchived ? (
                            <span className="badge bg-slate-200 text-slate-650 border border-slate-300 font-bold font-mono">
                              <Clock className="w-3.5 h-3.5 text-slate-500" /> ARCHIVED
                            </span>
                          ) : (
                            <StatusBadge status={campaign.status} type="campaign" />
                          )
                        ) : (
                          <StatusBadge status={event.status} type="event" />
                        )}
                      </div>
                      <p className="text-xs text-slate-500 mt-0.5 truncate font-mono">
                        {formattedEventDateStr} • {campaign ? campaign.campaignPost : event.description}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    {hasCampaign && (
                      <span className="text-[11px] font-bold text-blue-600 bg-blue-50 px-3 py-1.5 rounded-xl border border-blue-200 shadow-sm">
                        Open Campaign &rarr;
                      </span>
                    )}

                    {isPending && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          triggerConfirmation({
                            title: 'Reject Event',
                            message: `Are you sure you want to reject the event "${event.title}"?`,
                            confirmLabel: 'Reject',
                            type: 'danger',
                            onConfirm: () => handleCancelEvent(event.id),
                          });
                        }}
                        className="deep-3d-press px-2.5 py-1.5 rounded-xl bg-red-600 hover:bg-red-700 text-white text-[11px] font-bold flex items-center gap-1 shadow-sm"
                      >
                        <XCircle className="w-3.5 h-3.5" /> Reject Event
                      </button>
                    )}

                    {isCancelled && !hasCampaign && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          triggerConfirmation({
                            title: 'Restore Event',
                            message: `Are you sure you want to restore the event "${event.title}" back to pending status?`,
                            confirmLabel: 'Restore',
                            type: 'success',
                            onConfirm: () => handleRestoreEvent(event.id),
                          });
                        }}
                        className="deep-3d-press px-2.5 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-[11px] font-bold flex items-center gap-1 shadow-sm"
                      >
                        <RotateCcw className="w-3.5 h-3.5" /> Restore Event
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Lightbox Modal */}
      {lightboxImageUrl && (
        <ImageLightboxModal imageUrl={lightboxImageUrl} onClose={() => setLightboxImageUrl(null)} />
      )}

      {/* Upload Modal */}
      {uploadModalEventId && (
        <ImageUploadModal
          eventId={uploadModalEventId}
          type="event"
          title={
            events.find((e) => e.id === uploadModalEventId)?.imageUrl
              ? 'Replace Event Poster Image'
              : 'Upload Event Poster Image'
          }
          onClose={() => setUploadModalEventId(null)}
          onUploadSuccess={(newImageUrl: string) => {
            if (uploadModalEventId) {
              saveLocalPoster(uploadModalEventId, newImageUrl);
            }
            showToast('Event poster uploaded successfully!', 'success');
            setEvents((prevEvents) =>
              prevEvents.map((evt) => (evt.id === uploadModalEventId ? { ...evt, imageUrl: newImageUrl } : evt))
            );
            fetchEventsAndCampaigns();
          }}
        />
      )}

      {/* Reusable Action Confirmation Dialog */}
      <ConfirmationDialog
        isOpen={confirmDialog.isOpen}
        title={confirmDialog.title}
        message={confirmDialog.message}
        confirmLabel={confirmDialog.confirmLabel}
        type={confirmDialog.type}
        isProcessing={isProcessing}
        onConfirm={async () => {
          setIsProcessing(true);
          try {
            await confirmDialog.onConfirm();
            setConfirmDialog((prev) => ({ ...prev, isOpen: false }));
          } catch (err) {
            console.error('Confirmation action failed:', err);
          } finally {
            setIsProcessing(false);
          }
        }}
        onCancel={() => setConfirmDialog((prev) => ({ ...prev, isOpen: false }))}
      />
    </PullToRefresh>
  );
};
