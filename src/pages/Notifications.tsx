import React, { useState, useEffect } from 'react';
import { Bell, RefreshCw, CheckCheck } from 'lucide-react';
import { NotificationItem, Campaign, SpeehiveEvent, PlatformPosting, NotificationType } from '../types';
import { apiClient } from '../api/client';
import { useAuth } from '../context/AuthContext';

interface NotificationsProps {
  onNavigateToCampaign: (campaignId: string) => void;
}

const LAST_VIEWED_KEY = 'hive_last_viewed_notifications_timestamp';

export const Notifications: React.FC<NotificationsProps> = ({ onNavigateToCampaign }) => {
  const { role } = useAuth();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Date format: "23 Jul 2026 • 05:19 PM"
  const formatNotificationDate = (dateStr?: string | null) => {
    if (!dateStr) return '';
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return dateStr;
      const day = d.getDate().toString().padStart(2, '0');
      const month = d.toLocaleString('en-US', { month: 'short' });
      const year = d.getFullYear();
      const time = d.toLocaleString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
      return `${day} ${month} ${year} • ${time}`;
    } catch (e) {
      return dateStr;
    }
  };

  const loadNotifications = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      // 1. Fetch Live Campaigns, Events, and (for Admins) Social Media Credentials
      const promises: Promise<any>[] = [
        apiClient.get('/api/Campaigns'),
        apiClient.get('/api/Events'),
      ];

      if (role === 'Admin') {
        promises.push(apiClient.get('/api/SocialMediaCredentials'));
      }

      const results = await Promise.allSettled(promises);
      const campaignRes = results[0];
      const eventRes = results[1];
      const credRes = role === 'Admin' ? results[2] : null;

      const liveCampaigns: Campaign[] =
        campaignRes.status === 'fulfilled' && Array.isArray(campaignRes.value.data)
          ? campaignRes.value.data
          : [];

      const liveEvents: SpeehiveEvent[] =
        eventRes.status === 'fulfilled' && Array.isArray(eventRes.value.data)
          ? eventRes.value.data
          : [];

      const credentialsList =
        credRes && credRes.status === 'fulfilled' && Array.isArray((credRes as any).value?.data)
          ? (credRes as any).value.data
          : [];

      const eventIdToTitleMap = new Map<string, string>();
      liveEvents.forEach((e) => eventIdToTitleMap.set(e.id, e.title));

      const lastViewedTimeStr = localStorage.getItem(LAST_VIEWED_KEY);
      const lastViewedTime = lastViewedTimeStr ? new Date(lastViewedTimeStr).getTime() : 0;

      const campaignNotifications: NotificationItem[] = [];
      const systemNotifications: NotificationItem[] = [];

      // 2. Process Cancelled Events (Normal Notification Card)
      liveEvents
        .filter((e) => e.status && e.status.toLowerCase() === 'cancelled')
        .forEach((e) => {
          const createdTime = e.startTime ? new Date(e.startTime).getTime() : 0;
          systemNotifications.push({
            id: `evt-${e.id}`,
            title: e.title,
            message: 'Event has been cancelled',
            timestamp: e.startTime || new Date().toISOString(),
            type: 'EVENT_CANCELLED',
            isRead: createdTime <= lastViewedTime,
            eventId: e.id,
            platformPostings: [],
          });
        });

      // 3. Process API Key Token Expiration System Reminders (Parity with NotificationWorker.kt)
      credentialsList.forEach((cred: any) => {
        if (!cred.expiresAt) return;
        const expires = new Date(cred.expiresAt).getTime();
        if (isNaN(expires)) return;
        const diffDays = Math.ceil((expires - Date.now()) / (1000 * 60 * 60 * 24));

        if (diffDays <= 7) {
          const isExpired = diffDays <= 0;
          systemNotifications.push({
            id: `token-warning-${cred.provider}`,
            title: `${cred.provider} Token Expiration Reminder`,
            message: isExpired
              ? `API token for ${cred.provider} HAS EXPIRED. Please update key in Dashboard.`
              : `API token for ${cred.provider} expires in ${diffDays} ${diffDays === 1 ? 'day' : 'days'}. Please update key in Dashboard.`,
            timestamp: cred.expiresAt,
            type: 'REVIEW_REQUIRED',
            isRead: false,
            platformPostings: [],
          });
        }
      });

      // 4. Process Live Campaigns from Backend
      await Promise.all(
        liveCampaigns.map(async (campaign) => {
          const displayTitle = eventIdToTitleMap.get(campaign.eventId) || `Campaign #${campaign.campaignId}`;
          const status = (campaign.status || '').toLowerCase();
          const createdTime = campaign.createdAt ? new Date(campaign.createdAt).getTime() : 0;
          const isRead = createdTime <= lastViewedTime;

          let notifType: NotificationType | null = null;
          let notifMessage = '';

          if (status === 'generated' || status === 'active') {
            notifType = 'REVIEW_REQUIRED';
            notifMessage = 'Campaign awaiting approval';
          } else if (status === 'approved') {
            notifType = 'APPROVED';
            notifMessage = 'Campaign approved successfully';
          } else if (status === 'rejected') {
            notifType = 'REJECTED';
            notifMessage = 'Campaign rejected by reviewer';
          } else if (status === 'posted' || status === 'published') {
            notifType = 'PUBLISHED';
            notifMessage = 'Campaign posted to social media channels';
          }

          let postings: PlatformPosting[] = [];

          // ONLY APPROVED or PUBLISHED notifications show platform postings matching mobile app logic
          if (notifType === 'APPROVED' || notifType === 'PUBLISHED') {
            try {
              const schedRes = await apiClient.get(`/api/Approval/${campaign.eventId}/schedule`);
              const data = schedRes.data || {};

              const linkedInTime =
                data.schdtimeLinkedIn || data.SchdtimeLinkedIn || data.schdtimeLinkedin || data.SchdTimeLinkedIn;
              const instagramTime =
                data.schdtimeInstagram || data.SchdtimeInstagram || data.schdtimeinstagram || data.SchdTimeInstagram;
              const teamsTime =
                data.schdtimeTeams || data.SchdtimeTeams || data.schdtimeteams || data.SchdTimeTeams;
              const whatsappTime =
                data.schdtimeWhatsapp || data.SchdtimeWhatsapp || data.schdtimeWhatsApp || data.SchdTimeWhatsapp;

              const isPublished = status === 'published' || status === 'posted' || Boolean(campaign.postedAt);

              postings = [
                {
                  platform: 'LinkedIn',
                  status: isPublished ? 'Posted' : linkedInTime ? 'Scheduled' : 'Pending',
                  postedAt: linkedInTime || campaign.postedAt || campaign.createdAt,
                },
                {
                  platform: 'Instagram',
                  status: isPublished ? 'Posted' : instagramTime ? 'Posted' : 'Pending',
                  postedAt: instagramTime || campaign.postedAt || campaign.createdAt,
                },
                {
                  platform: 'Teams',
                  status: isPublished ? 'Posted' : teamsTime ? 'Posted' : 'Pending',
                  postedAt: teamsTime || campaign.postedAt || campaign.createdAt,
                },
                {
                  platform: 'WhatsApp',
                  status: isPublished ? 'Posted' : whatsappTime ? 'Posted' : 'Pending',
                  postedAt: whatsappTime || campaign.postedAt || campaign.createdAt,
                },
              ];
            } catch (e) {
              const isPublished = status === 'published' || status === 'posted' || Boolean(campaign.postedAt);
              const defaultStatus = isPublished ? 'Posted' : 'Pending';
              postings = [
                { platform: 'LinkedIn', status: isPublished ? 'Posted' : 'Scheduled', postedAt: campaign.postedAt || campaign.createdAt },
                { platform: 'Instagram', status: defaultStatus, postedAt: campaign.postedAt || campaign.createdAt },
                { platform: 'Teams', status: defaultStatus, postedAt: campaign.postedAt || campaign.createdAt },
                { platform: 'WhatsApp', status: defaultStatus, postedAt: campaign.postedAt || campaign.createdAt },
              ];
            }
          }

          if (notifType) {
            campaignNotifications.push({
              id: `cmp-${campaign.campaignId || campaign.eventId}`,
              title: displayTitle,
              message: notifMessage,
              timestamp: campaign.postedAt || campaign.createdAt || new Date().toISOString(),
              type: notifType,
              isRead: isRead,
              eventId: campaign.eventId,
              campaignId: campaign.eventId,
              platformPostings: postings,
            });
          }
        })
      );

      // Sort by timestamp descending
      const sorted = [...campaignNotifications, ...systemNotifications].sort((a, b) => {
        const timeA = new Date(a.timestamp).getTime();
        const timeB = new Date(b.timestamp).getTime();
        return timeB - timeA;
      });

      setNotifications(sorted);
    } catch (err: any) {
      console.error('Error loading live notifications:', err);
      setErrorMessage('Failed to load notifications from backend.');
      setNotifications([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
    localStorage.setItem(LAST_VIEWED_KEY, new Date().toISOString());
  }, []);

  const markAllRead = () => {
    localStorage.setItem(LAST_VIEWED_KEY, new Date().toISOString());
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  };

  return (
    <div className="space-y-6 pb-16 max-w-4xl mx-auto">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-3 font-heading">
            <Bell className="w-8 h-8 text-blue-600" />
            <span>Notification Center</span>
          </h1>
          <p className="text-sm font-medium text-slate-500 mt-1">
            Real-time campaign status alerts, platform posting summaries, and system warnings
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button onClick={loadNotifications} className="deep-3d-press btn-secondary text-xs">
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
          <button onClick={markAllRead} className="deep-3d-press btn-secondary text-xs">
            <CheckCheck className="w-4 h-4 text-emerald-600" />
            Mark All Read
          </button>
        </div>
      </div>

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 text-xs font-bold border border-red-200">
          {errorMessage}
        </div>
      )}

      {/* Notifications List */}
      {isLoading ? (
        <div className="p-12 text-center">
          <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-sm font-semibold text-slate-600 font-mono">Loading live notifications from database...</p>
        </div>
      ) : notifications.length === 0 ? (
        <div className="deep-3d-card p-12 text-center bg-white">
          <Bell className="w-12 h-12 text-slate-400 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-800 font-heading">No notifications found</h3>
          <p className="text-xs text-slate-500 mt-1 font-mono">0 active notifications returned by backend database.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {notifications.map((notification) => {
            const isApproved = notification.type === 'APPROVED';
            const isRejected = notification.type === 'REJECTED';
            const isReview = notification.type === 'REVIEW_REQUIRED';

            const typeBadgeStyle = isApproved
              ? 'bg-emerald-100 text-emerald-700'
              : isRejected
              ? 'bg-red-100 text-red-700'
              : isReview
              ? 'bg-amber-100 text-amber-700'
              : 'bg-blue-100 text-blue-700';

            return (
              <div
                key={notification.id}
                onClick={() => notification.campaignId && onNavigateToCampaign(notification.campaignId)}
                className="deep-3d-card p-6 bg-white space-y-4 border border-slate-200/80 rounded-2xl shadow-sm hover:border-slate-300 transition-all cursor-pointer"
              >
                {/* Title and Top Timestamp Row */}
                <div className="flex items-start justify-between gap-4">
                  <h3 className="font-extrabold text-lg text-slate-900 tracking-tight font-heading leading-tight">
                    {notification.title}
                  </h3>
                  <span className="text-[11px] font-medium text-slate-600 font-mono shrink-0">
                    {formatNotificationDate(notification.timestamp)}
                  </span>
                </div>

                {/* Subtitle Message */}
                <p className="text-xs font-medium text-slate-700 font-mono">{notification.message}</p>

                {/* Status Pill Badge */}
                <div>
                  <span className={`inline-block text-xs font-bold font-mono px-3 py-1 rounded-lg uppercase ${typeBadgeStyle}`}>
                    {notification.type.replace('_', ' ')}
                  </span>
                </div>

                {/* Platform Postings Section (ONLY rendered for APPROVED or PUBLISHED status) */}
                {notification.platformPostings && notification.platformPostings.length > 0 && (
                  <div className="pt-4 border-t border-slate-100 space-y-3">
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest font-mono">
                      PLATFORM POSTINGS
                    </p>

                    <div className="space-y-2.5">
                      {notification.platformPostings.map((posting, idx) => {
                        const isPosted = (posting.status || '').toLowerCase() === 'posted';
                        const isScheduled = (posting.status || '').toLowerCase() === 'scheduled';
                        const isFailed = (posting.status || '').toLowerCase() === 'failed';

                        const statusPillStyle = isPosted
                          ? 'bg-emerald-100 text-emerald-700'
                          : isScheduled
                          ? 'bg-amber-100 text-amber-700'
                          : isFailed
                          ? 'bg-red-100 text-red-700'
                          : 'bg-blue-100 text-blue-700';

                        return (
                          <div
                            key={idx}
                            className="p-3.5 rounded-2xl bg-slate-50/70 border border-slate-100 flex items-center justify-between gap-4"
                          >
                            <div className="flex items-center gap-3">
                              <span className="font-bold text-sm text-slate-900 font-heading">
                                {posting.platform}
                              </span>
                              <span className={`text-[11px] font-bold font-mono px-2.5 py-0.5 rounded-md ${statusPillStyle}`}>
                                {posting.status}
                              </span>
                            </div>

                            {posting.postedAt && (
                              <span className="text-[11px] font-medium text-slate-500 font-mono shrink-0">
                                {formatNotificationDate(posting.postedAt)}
                              </span>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
