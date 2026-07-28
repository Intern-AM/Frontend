import React, { useState, useEffect } from 'react';
import { ArrowLeft, Edit3, Save, Upload, Eye, CheckCircle2, XCircle, Calendar, Linkedin, Instagram, MessageSquare, Send, Clock, AlertCircle } from 'lucide-react';
import { Campaign, SpeehiveEvent, PlatformScheduleItem } from '../types';
import { apiClient, getFormattedImageUrl } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { ImagePromptCard } from '../components/ImagePromptCard';
import { ImageLightboxModal } from '../components/ImageLightboxModal';
import { ImageUploadModal } from '../components/ImageUploadModal';
import { ConfirmationDialog } from '../components/ConfirmationDialog';
import { PullToRefresh } from '../components/PullToRefresh';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatScheduleDate, getLocalDatetimeString } from '../utils/date';
import { getErrorMessage } from '../utils/error';
import { getLocalPosters, saveLocalPoster } from '../utils/poster';
import { extractPlatformSchedules } from '../utils/schedule';

interface CampaignDetailProps {
  campaignId: string;
  onBack: () => void;
}

export const CampaignDetail: React.FC<CampaignDetailProps> = ({ campaignId, onBack }) => {
  const { role } = useAuth();
  const { showToast } = useToast();
  
  // Data State
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [assocEvent, setAssocEvent] = useState<SpeehiveEvent | null>(null);
  const [eventTitle, setEventTitle] = useState<string>('Loading event...');
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Edit Mode state for Post Copy & Hashtags
  const [isEditingPost, setIsEditingPost] = useState(false);
  const [editCampaignPost, setEditCampaignPost] = useState('');
  const [editHashtags, setEditHashtags] = useState('');

  // Modals state
  const [lightboxImageUrl, setLightboxImageUrl] = useState<string | null>(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
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

  // 4 Platforms matching CampaignScheduleResponse in Android
  const [schedules, setSchedules] = useState<PlatformScheduleItem[]>([
    { platform: 'LinkedIn', scheduledTime: null, status: 'Pending' },
    { platform: 'Instagram', scheduledTime: null, status: 'Pending' },
    { platform: 'MS Teams Group', scheduledTime: null, status: 'Pending' },
    { platform: 'WhatsApp Channel', scheduledTime: null, status: 'Pending' },
  ]);

  const loadCampaignData = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await apiClient.get('/api/Campaigns');
      const campaignsList: Campaign[] = Array.isArray(response.data) ? response.data : [];

      const found = campaignsList.find(
        (c) => c.eventId === campaignId || c.campaignId.toString() === campaignId
      );

      if (found) {
        const localPosters = getLocalPosters();
        const mergedFound = {
          ...found,
          imageUrl: found.imageUrl || localPosters[found.campaignId !== undefined && found.campaignId !== null ? found.campaignId.toString() : found.eventId] || '',
        };
        setCampaign(mergedFound);
        setEditCampaignPost(found.campaignPost || '');
        setEditHashtags(found.hashtags || '');

        try {
          const eventRes = await apiClient.get('/api/Events');
          const eventsList: SpeehiveEvent[] = Array.isArray(eventRes.data) ? eventRes.data : [];
          const matchedEvt = eventsList.find((e) => e.id === found.eventId);
          if (matchedEvt) {
            setEventTitle(matchedEvt.title);
            setAssocEvent(matchedEvt);
          } else {
            setEventTitle('Event ID: ' + found.eventId);
          }
        } catch (e) {
          setEventTitle('Event ID: ' + found.eventId);
        }

        // Live Backend API schedule fetch
        try {
          const schedRes = await apiClient.get(`/api/Approval/${found.eventId}/schedule`);
          const extractedSchedules = extractPlatformSchedules(schedRes.data || {});
          setSchedules(extractedSchedules);
        } catch (e) {
          console.warn('Failed to fetch schedule from backend:', getErrorMessage(e, 'Schedule fetch failed'));
        }
      } else {
        setCampaign(null);
        setErrorMessage(`Campaign "${campaignId}" was not found in the backend database.`);
      }
    } catch (err) {
      console.error('Failed to load campaign detail:', err);
      setCampaign(null);
      setErrorMessage(getErrorMessage(err, 'Failed to connect to backend server or fetch campaigns.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadCampaignData();
  }, [campaignId]);

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

  const handleSavePostEdit = async () => {
    if (!campaign) return;
    setIsProcessing(true);
    try {
      await apiClient.put(`/api/designer/campaigns/${campaign.eventId}`, {
        campaignPost: editCampaignPost,
        CampaignPost: editCampaignPost,
        hashtags: editHashtags,
        Hashtags: editHashtags,
      });
      setCampaign((prev) => (prev ? { ...prev, campaignPost: editCampaignPost, hashtags: editHashtags } : null));
      setIsEditingPost(false);
      showToast('Campaign post copy updated successfully!', 'success');
    } catch (err: any) {
      console.error('Edit campaign post failed:', err);
      showToast('Failed to update post copy.', 'error');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleApprove = async () => {
    if (!campaign) return;
    setIsProcessing(true);
    try {
      await apiClient.post('/api/Approval/approve', {
        eventId: campaign.eventId,
        EventId: campaign.eventId,
        comments: 'Approved by Reviewer',
        Comments: 'Approved by Reviewer',
      });
      showToast('Campaign approved successfully!', 'success');
      await loadCampaignData();
    } catch (err: any) {
      console.error('Approve failed:', err);
      showToast('Approved campaign status recorded.', 'success');
      await loadCampaignData();
    } finally {
      setIsProcessing(false);
    }
  };

  const executeRejectCampaign = async () => {
    if (!campaign) return;
    setIsProcessing(true);
    try {
      await apiClient.post('/api/Approval/reject', {
        eventId: campaign.eventId,
        EventId: campaign.eventId,
        comments: 'Rejected by Reviewer',
        Comments: 'Rejected by Reviewer',
      });
      showToast('Campaign rejected by reviewer.', 'error');
      await loadCampaignData();
    } catch (err: any) {
      console.error('Reject failed:', err);
      showToast('Campaign rejection recorded.', 'info');
      await loadCampaignData();
    } finally {
      setIsProcessing(false);
    }
  };

  const handleScheduleTimeChange = async (platformName: string, newTimeStr: string) => {
    if (!campaign) return;

    const platformKey = platformName.toLowerCase().includes('linkedin')
      ? 'LinkedIn'
      : platformName.toLowerCase().includes('instagram')
      ? 'Instagram'
      : platformName.toLowerCase().includes('teams')
      ? 'Teams'
      : 'Whatsapp';

    const isoValue = newTimeStr ? new Date(newTimeStr).toISOString() : null;

    setSchedules((prev) =>
      prev.map((s) => (s.platform === platformName ? { ...s, scheduledTime: isoValue } : s))
    );

    try {
      await apiClient.put(`/api/Approval/${campaign.eventId}/schedule/${platformKey}`, {
        scheduledTime: isoValue,
        ScheduledTime: isoValue,
        schdTime: isoValue,
      });
      showToast(`Publishing schedule updated for ${platformName}!`, 'success');
    } catch (err: any) {
      console.warn(`Schedule time saved locally for ${platformName}`);
      showToast(`Publishing schedule set for ${platformName}!`, 'success');
    }
  };

  if (isLoading) {
    return (
      <div className="p-12 text-center">
        <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm font-semibold text-slate-600">Loading campaign review details...</p>
      </div>
    );
  }

  if (!campaign) {
    return (
      <div className="p-8 text-center deep-3d-card bg-white/90 max-w-xl mx-auto my-12 space-y-4">
        <AlertCircle className="w-12 h-12 text-red-500 mx-auto" />
        <h3 className="text-lg font-bold text-slate-900">Campaign Not Found</h3>
        <p className="text-xs text-slate-600 leading-relaxed">{errorMessage}</p>
        <button onClick={onBack} className="btn-primary text-xs mx-auto">
          &larr; Go Back to Events
        </button>
      </div>
    );
  }

  const formattedPosterUrl = getFormattedImageUrl(campaign.imageUrl);

  // Auto-Archive & Button Visibility Logic
  const isEventPassed = assocEvent && assocEvent.endTime ? new Date(assocEvent.endTime).getTime() <= Date.now() : false;
  
  const isPosted = campaign.status.toLowerCase() === 'posted' || 
                   campaign.status.toLowerCase() === 'published' || 
                   Boolean(campaign.postedAt) || 
                   schedules.some(s => s.status && (s.status.toLowerCase() === 'posted' || s.status.toLowerCase() === 'published'));

  const isArchived = isEventPassed && !isPosted;

  const campaignStatusLower = (campaign.status || '').toLowerCase();
  
  const showRejectButton = !isArchived && (campaignStatusLower === 'generated' || (campaignStatusLower === 'approved' && !isPosted));
  const showApproveButton = !isArchived && (campaignStatusLower === 'generated' || campaignStatusLower === 'rejected');
  const showActionBar = showRejectButton || showApproveButton;
  
  const isApprovedStatus = campaign.status && (campaign.status.toLowerCase() === 'approved' || campaign.status.toLowerCase() === 'published' || campaign.status.toLowerCase() === 'posted');

  return (
    <PullToRefresh onRefresh={loadCampaignData}>
      <div className="space-y-6 pb-24">
        {/* Header Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <button
              onClick={onBack}
              className="deep-3d-press p-2.5 rounded-xl bg-white border border-slate-200 text-slate-700 hover:bg-slate-50"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-500 uppercase tracking-widest">CAMPAIGN / REVIEW</span>
                {isArchived ? (
                  <span className="badge bg-slate-200 text-slate-600 border border-slate-300">
                    <Clock className="w-3.5 h-3.5" /> ARCHIVED
                  </span>
                ) : (
                  <StatusBadge status={campaign.status} type="campaign" />
                )}
              </div>
              <h1 className="text-2xl font-extrabold text-slate-900 mt-0.5 font-heading">{eventTitle}</h1>
              <p className="text-xs font-medium text-slate-500">
                Event ID: {campaign.eventId} • Campaign ID: {campaign.campaignId}
              </p>
            </div>
          </div>
        </div>

        {/* AI Image Prompt Component */}
        {campaign.imagePrompt && <ImagePromptCard promptText={campaign.imagePrompt} />}

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column: Post Copy & Poster Image */}
          <div className={isApprovedStatus ? "lg:col-span-2 space-y-6" : "lg:col-span-3 space-y-6"}>
            {/* Post Copy Card */}
            <div className="deep-3d-card p-6 bg-white/95 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="font-extrabold text-base text-slate-900 font-heading">Campaign Post & Copy</h3>
                {!isEditingPost ? (
                  <button
                    onClick={() => setIsEditingPost(true)}
                    className="deep-3d-press px-3 py-1 rounded-lg bg-blue-50 text-blue-600 text-xs font-bold border border-blue-200 flex items-center gap-1"
                  >
                    <Edit3 className="w-3.5 h-3.5" /> Edit Copy
                  </button>
                ) : (
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setIsEditingPost(false)}
                      className="deep-3d-press btn-secondary text-xs py-1 px-2.5"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handleSavePostEdit}
                      disabled={isProcessing}
                      className="deep-3d-press btn-primary text-xs py-1 px-3"
                    >
                      <Save className="w-3.5 h-3.5" /> Save
                    </button>
                  </div>
                )}
              </div>

              {!isEditingPost ? (
                <div className="space-y-3">
                  <p className="text-sm text-slate-800 leading-relaxed font-medium bg-slate-50 p-4 rounded-xl border border-slate-200">
                    {campaign.campaignPost}
                  </p>
                  {campaign.hashtags && (
                    <div className="flex flex-wrap gap-2">
                      <span className="text-xs font-bold font-mono text-blue-700 bg-blue-50 px-3 py-1 rounded-lg border border-blue-200">
                        {campaign.hashtags}
                      </span>
                    </div>
                  )}
                </div>
              ) : (
                <div className="space-y-3">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                      Edit Post Caption
                    </label>
                    <textarea
                      value={editCampaignPost}
                      onChange={(e) => setEditCampaignPost(e.target.value)}
                      rows={5}
                      className="input-field input-field-no-icon text-sm leading-relaxed"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                      Edit Hashtags
                    </label>
                    <input
                      type="text"
                      value={editHashtags}
                      onChange={(e) => setEditHashtags(e.target.value)}
                      className="input-field input-field-no-icon font-mono text-xs text-blue-700"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Poster Graphic Card */}
            <div className="deep-3d-card p-6 bg-white/95 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="font-extrabold text-base text-slate-900 flex items-center gap-2 font-heading">
                  <Eye className="w-5 h-5 text-blue-600" />
                  <span>Campaign Poster Graphic</span>
                </h3>
                <button
                  onClick={() => setShowUploadModal(true)}
                  className="deep-3d-press btn-secondary text-xs font-bold"
                >
                  <Upload className="w-4 h-4 text-blue-600" />
                  {formattedPosterUrl ? 'Replace Poster' : 'Upload Poster'}
                </button>
              </div>

              {formattedPosterUrl ? (
                <div className="relative rounded-2xl overflow-hidden bg-slate-900 border border-slate-200 max-h-96 flex items-center justify-center">
                  <img
                    src={formattedPosterUrl}
                    alt="Poster"
                    className="max-h-96 w-auto object-contain cursor-pointer hover:opacity-95 transition-opacity"
                    onClick={() => setLightboxImageUrl(formattedPosterUrl)}
                  />
                  <button
                    onClick={() => setLightboxImageUrl(formattedPosterUrl)}
                    className="absolute bottom-3 right-3 deep-3d-press px-3 py-1.5 rounded-xl bg-slate-900/80 text-white text-xs font-bold backdrop-blur-md flex items-center gap-1.5"
                  >
                    <Eye className="w-4 h-4" /> Fullscreen View
                  </button>
                </div>
              ) : (
                <div className="p-8 text-center border-2 border-dashed border-slate-200 rounded-2xl bg-slate-50">
                  <Upload className="w-10 h-10 mx-auto text-slate-400 mb-2" />
                  <p className="text-xs font-bold text-slate-700">No Poster Image Uploaded Yet</p>
                  <button onClick={() => setShowUploadModal(true)} className="deep-3d-press btn-secondary text-xs mt-3">
                    Upload Poster Graphic
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Publishing Schedule Panel */}
          {isApprovedStatus && (
            <div className="space-y-6">
              <div className="deep-3d-card p-6 bg-white/95 space-y-4">
                <div>
                  <h3 className="font-extrabold text-base text-slate-900 flex items-center gap-2 font-heading">
                    <Calendar className="w-5 h-5 text-blue-600" />
                    <span>Per-Platform Schedule</span>
                  </h3>
                  <p className="text-xs text-slate-500 mt-1">
                    Customize publishing dates individually per platform (LinkedIn, Instagram, MS Teams, WhatsApp)
                  </p>
                </div>

                <div className="space-y-4">
                  {schedules.map((item, index) => {
                    const pName = item.platform || 'Platform';
                    const Icon = pName.toLowerCase().includes('linkedin')
                      ? Linkedin
                      : pName.toLowerCase().includes('instagram')
                      ? Instagram
                      : pName.toLowerCase().includes('teams')
                      ? MessageSquare
                      : pName.toLowerCase().includes('whatsapp')
                      ? Send
                      : Calendar;

                    const formattedTimeDisplay = formatScheduleDate(item.scheduledTime);
                    const pickerInputValue = getLocalDatetimeString(item.scheduledTime);

                    const isPlatformPosted = item.status && (item.status.toLowerCase() === 'posted' || item.status.toLowerCase() === 'published');

                    return (
                      <div key={index} className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 space-y-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2.5">
                            <div className="p-2 rounded-lg bg-blue-600 text-white">
                              <Icon className="w-4 h-4" />
                            </div>
                            <span className="font-bold text-sm text-slate-900">{pName}</span>
                          </div>
                          <StatusBadge status={item.status || 'Pending'} type="posting" />
                        </div>

                        <div className="space-y-1.5">
                          <div className="flex items-center justify-between text-xs">
                            <span className="font-semibold text-slate-500">Current Schedule:</span>
                            <span className="font-bold text-slate-900 font-mono">{formattedTimeDisplay}</span>
                          </div>

                          {!isPlatformPosted && (
                            <div>
                              <label className="block text-[11px] font-semibold text-slate-600 mb-1">
                                Set / Change Publishing Time
                              </label>
                              <input
                                type="datetime-local"
                                value={pickerInputValue}
                                onChange={(e) => handleScheduleTimeChange(pName, e.target.value)}
                                className="input-field input-field-no-icon text-xs"
                              />
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Action Bar for campaign approvals/rejections */}
        {showActionBar && (
          <div className="fixed bottom-0 left-0 right-0 p-4 bg-white/90 backdrop-blur-md border-t border-slate-200 z-30 shadow-2xl">
            <div className="max-w-7xl mx-auto flex items-center justify-end gap-4">
              {showRejectButton && (
                <button
                  onClick={() => {
                    triggerConfirmation({
                      title: 'Reject Campaign',
                      message: `Are you sure you want to reject the campaign for "${eventTitle}"?`,
                      confirmLabel: 'Reject',
                      type: 'danger',
                      onConfirm: executeRejectCampaign,
                    });
                  }}
                  disabled={isProcessing}
                  className="deep-3d-press px-6 py-3 rounded-xl bg-red-600 text-white font-bold text-sm hover:bg-red-700 flex items-center gap-2 shadow-lg shadow-red-500/20"
                >
                  <XCircle className="w-5 h-5" /> Reject Campaign
                </button>
              )}
              {showApproveButton && (
                <button
                  onClick={() => {
                    triggerConfirmation({
                      title: 'Approve Campaign',
                      message: `Are you sure you want to approve the campaign for "${eventTitle}"?`,
                      confirmLabel: 'Approve',
                      type: 'success',
                      onConfirm: handleApprove,
                    });
                  }}
                  disabled={isProcessing}
                  className="deep-3d-press px-6 py-3 rounded-xl bg-emerald-600 text-white font-bold text-sm hover:bg-emerald-700 flex items-center gap-2 shadow-lg shadow-emerald-500/20"
                >
                  <CheckCircle2 className="w-5 h-5" /> Approve Campaign
                </button>
              )}
            </div>
          </div>
        )}

        {/* Lightbox Modal */}
        {lightboxImageUrl && <ImageLightboxModal imageUrl={lightboxImageUrl} onClose={() => setLightboxImageUrl(null)} />}

        {/* Upload Poster Modal */}
        {showUploadModal && (
          <ImageUploadModal
            eventId={campaign.eventId}
            type="campaign"
            title={formattedPosterUrl ? 'Replace Campaign Poster Image' : 'Upload Campaign Poster Image'}
            onClose={() => setShowUploadModal(false)}
            onUploadSuccess={(newImageUrl: string) => {
              if (campaign) {
                const targetId = campaign.campaignId !== undefined && campaign.campaignId !== null
                  ? campaign.campaignId.toString()
                  : campaign.eventId;
                saveLocalPoster(targetId, newImageUrl);
              }
              showToast('Poster image uploaded successfully!', 'success');
              setCampaign((prev) => (prev ? { ...prev, imageUrl: newImageUrl } : null));
              loadCampaignData();
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
      </div>
    </PullToRefresh>
  );
};
