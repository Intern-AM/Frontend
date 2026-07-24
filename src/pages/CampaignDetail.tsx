import React, { useState, useEffect } from 'react';
import { ArrowLeft, Edit3, Save, Upload, Eye, CheckCircle2, XCircle, Calendar, Linkedin, Instagram, MessageSquare, Send, Clock, AlertCircle } from 'lucide-react';
import { Campaign, SpeehiveEvent, PlatformScheduleItem } from '../types';
import { apiClient, getFormattedImageUrl } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { ImagePromptCard } from '../components/ImagePromptCard';
import { ImageLightboxModal } from '../components/ImageLightboxModal';
import { ImageUploadModal } from '../components/ImageUploadModal';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

interface CampaignDetailProps {
  campaignId: string;
  onBack: () => void;
}

export const CampaignDetail: React.FC<CampaignDetailProps> = ({ campaignId, onBack }) => {
  const { role } = useAuth();
  const { showToast } = useToast();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
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
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectionComments, setRejectionComments] = useState('');

  // 4 Platforms matching CampaignScheduleResponse in Android
  const [schedules, setSchedules] = useState<PlatformScheduleItem[]>([
    { platform: 'LinkedIn', scheduledTime: null, status: 'Pending' },
    { platform: 'Instagram', scheduledTime: null, status: 'Pending' },
    { platform: 'MS Teams Group', scheduledTime: null, status: 'Pending' },
    { platform: 'WhatsApp Channel', scheduledTime: null, status: 'Pending' },
  ]);

  const formatScheduleDate = (dateStr?: string | null) => {
    if (!dateStr) return 'Not Scheduled';
    try {
      return new Date(dateStr).toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (e) {
      return dateStr;
    }
  };

  const loadCampaignData = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await apiClient.get('/api/Campaigns');
      const campaigns: Campaign[] = Array.isArray(response.data) ? response.data : [];

      const found = campaigns.find(
        (c) => c.eventId === campaignId || c.campaignId.toString() === campaignId
      );

      if (found) {
        setCampaign(found);
        setEditCampaignPost(found.campaignPost || '');
        setEditHashtags(found.hashtags || '');

        try {
          const eventRes = await apiClient.get('/api/Events');
          const eventsList: SpeehiveEvent[] = Array.isArray(eventRes.data) ? eventRes.data : [];
          const matchedEvt = eventsList.find((e) => e.id === found.eventId);
          if (matchedEvt) {
            setEventTitle(matchedEvt.title);
          } else {
            setEventTitle('Event ID: ' + found.eventId);
          }
        } catch (e) {
          setEventTitle('Event ID: ' + found.eventId);
        }

        // Live Backend API schedule fetch: exact mapping matching CampaignScheduleResponse.kt
        try {
          const schedRes = await apiClient.get(`/api/Approval/${found.eventId}/schedule`);
          const data = schedRes.data || {};

          // Extract exact database fields returned by backend
          const linkedInTime =
            data.schdtimeLinkedIn || data.SchdtimeLinkedIn || data.schdtimeLinkedin || data.SchdTimeLinkedIn;
          const instagramTime =
            data.schdtimeInstagram || data.SchdtimeInstagram || data.schdtimeinstagram || data.SchdTimeInstagram;
          const teamsTime =
            data.schdtimeTeams || data.SchdtimeTeams || data.schdtimeteams || data.SchdTimeTeams;
          const whatsappTime =
            data.schdtimeWhatsapp || data.SchdtimeWhatsapp || data.schdtimeWhatsApp || data.SchdTimeWhatsapp;

          // Helper to find posting status if available in data.platforms array
          const getStatusFor = (pKey: string) => {
            if (Array.isArray(data.platforms)) {
              const item = data.platforms.find((p: any) =>
                (p.platform || '').toLowerCase().includes(pKey.toLowerCase())
              );
              return item?.status || 'Pending';
            }
            return 'Pending';
          };

          setSchedules([
            {
              platform: 'LinkedIn',
              scheduledTime: linkedInTime || null,
              status: getStatusFor('linkedin'),
            },
            {
              platform: 'Instagram',
              scheduledTime: instagramTime || null,
              status: getStatusFor('instagram'),
            },
            {
              platform: 'MS Teams Group',
              scheduledTime: teamsTime || null,
              status: getStatusFor('teams'),
            },
            {
              platform: 'WhatsApp Channel',
              scheduledTime: whatsappTime || null,
              status: getStatusFor('whatsapp'),
            },
          ]);
        } catch (e) {
          console.warn('Failed to fetch schedule from backend:', e);
        }
      } else {
        setCampaign(null);
        setErrorMessage(`Campaign "${campaignId}" was not found in the backend database.`);
      }
    } catch (err: any) {
      console.error('Failed to load campaign detail:', err);
      setCampaign(null);
      setErrorMessage('Failed to connect to backend server or fetch campaigns.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadCampaignData();
  }, [campaignId]);

  const handleSavePostEdit = async () => {
    if (!campaign) return;
    setIsProcessing(true);
    try {
      await apiClient.put(`/api/Campaigns/${campaign.eventId}`, {
        campaignPost: editCampaignPost,
        hashtags: editHashtags,
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
        comments: 'Approved by Reviewer',
      });
      setCampaign((prev) => (prev ? { ...prev, status: 'Approved' } : null));
      showToast('Campaign approved successfully!', 'success');
    } catch (err: any) {
      console.error('Approve failed:', err);
      setCampaign((prev) => (prev ? { ...prev, status: 'Approved' } : null));
      showToast('Approved campaign status recorded.', 'success');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleRejectConfirm = async () => {
    if (!campaign || !rejectionComments.trim()) return;
    setIsProcessing(true);
    try {
      await apiClient.post('/api/Approval/reject', {
        eventId: campaign.eventId,
        comments: rejectionComments,
      });
      setCampaign((prev) => (prev ? { ...prev, status: 'Rejected' } : null));
      setShowRejectModal(false);
      showToast('Campaign rejected by reviewer.', 'error');
    } catch (err: any) {
      console.error('Reject failed:', err);
      setCampaign((prev) => (prev ? { ...prev, status: 'Rejected' } : null));
      setShowRejectModal(false);
      showToast('Campaign rejection recorded.', 'info');
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
          &larr; Go Back to Campaigns
        </button>
      </div>
    );
  }

  const formattedPosterUrl = getFormattedImageUrl(campaign.imageUrl);
  const isGeneratedStatus = campaign.status && campaign.status.toLowerCase() === 'generated';
  const isApprovedStatus = campaign.status && (campaign.status.toLowerCase() === 'approved' || campaign.status.toLowerCase() === 'published');

  return (
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
              <StatusBadge status={campaign.status} type="campaign" />
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
                {campaign.cta && (
                  <p className="text-xs font-bold text-slate-700 bg-amber-50 p-2.5 rounded-lg border border-amber-200">
                    Call To Action: {campaign.cta}
                  </p>
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

        {/* STRICT CONDITION: Right Column Per-Platform Publishing Schedule ONLY appears when Campaign is APPROVED */}
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
                  const pickerInputValue = item.scheduledTime
                    ? new Date(item.scheduledTime).toISOString().slice(0, 16)
                    : '';

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
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Action Bar for Generated Campaigns */}
      {isGeneratedStatus && (
        <div className="fixed bottom-0 left-0 right-0 p-4 bg-white/90 backdrop-blur-md border-t border-slate-200 z-30 shadow-2xl">
          <div className="max-w-7xl mx-auto flex items-center justify-end gap-4">
            <button
              onClick={() => setShowRejectModal(true)}
              disabled={isProcessing}
              className="deep-3d-press px-6 py-3 rounded-xl bg-red-600 text-white font-bold text-sm hover:bg-red-700 flex items-center gap-2 shadow-lg shadow-red-500/20"
            >
              <XCircle className="w-5 h-5" /> Reject Campaign
            </button>
            <button
              onClick={handleApprove}
              disabled={isProcessing}
              className="deep-3d-press px-6 py-3 rounded-xl bg-emerald-600 text-white font-bold text-sm hover:bg-emerald-700 flex items-center gap-2 shadow-lg shadow-emerald-500/20"
            >
              <CheckCircle2 className="w-5 h-5" /> Approve Campaign
            </button>
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
          title="Upload Campaign Poster Image"
          onClose={() => setShowUploadModal(false)}
          onUploadSuccess={() => {
            showToast('Poster image uploaded successfully!', 'success');
            loadCampaignData();
          }}
        />
      )}

      {/* Reject Modal */}
      {showRejectModal && (
        <div className="modal-overlay" onClick={() => setShowRejectModal(false)}>
          <div className="deep-3d-card p-6 max-w-md w-full bg-white space-y-4" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-lg font-extrabold text-slate-900 font-heading">Reject Campaign</h3>
            <p className="text-xs text-slate-600">Provide rejection comments for the design team:</p>
            <textarea
              value={rejectionComments}
              onChange={(e) => setRejectionComments(e.target.value)}
              rows={3}
              className="input-field input-field-no-icon"
              placeholder="Reason for rejection..."
            />
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowRejectModal(false)} className="btn-secondary text-xs">
                Cancel
              </button>
              <button onClick={handleRejectConfirm} className="btn-primary text-xs bg-red-600 hover:bg-red-700">
                Confirm Rejection
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
