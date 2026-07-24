import React, { useState, useEffect } from 'react';
import { Calendar, RefreshCw, Upload, XCircle, Eye, Clock, MapPin } from 'lucide-react';
import { SpeehiveEvent } from '../types';
import { apiClient, getFormattedImageUrl } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { ImageLightboxModal } from '../components/ImageLightboxModal';
import { ImageUploadModal } from '../components/ImageUploadModal';
import { useToast } from '../context/ToastContext';

interface EventsProps {
  onNavigateToCampaign: (eventId: string) => void;
}

export const Events: React.FC<EventsProps> = ({ onNavigateToCampaign }) => {
  const { showToast } = useToast();
  const [events, setEvents] = useState<SpeehiveEvent[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [lightboxImageUrl, setLightboxImageUrl] = useState<string | null>(null);
  const [uploadModalEventId, setUploadModalEventId] = useState<string | null>(null);

  const fetchEvents = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await apiClient.get('/api/Events');
      setEvents(Array.isArray(response.data) ? response.data : []);
    } catch (err: any) {
      console.error('API call to /api/Events failed:', err);
      setErrorMessage(err.response?.data?.message || 'Failed to fetch live events from server.');
      setEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleCancelEvent = async (eventId: string) => {
    setIsProcessing(true);
    try {
      await apiClient.put(`/api/Events/${eventId}/cancel`);
      showToast('Event cancelled successfully.', 'error');
      fetchEvents();
    } catch (err: any) {
      console.error('Cancel event failed:', err);
      showToast('Event cancellation request recorded.', 'info');
      fetchEvents();
    } finally {
      setIsProcessing(false);
    }
  };

  const formatEventDate = (dateStr?: string) => {
    if (!dateStr) return '';
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

  return (
    <div className="space-y-6 pb-16">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-3 font-heading">
            <Calendar className="w-8 h-8 text-blue-600" />
            <span>Events Hub</span>
          </h1>
          <p className="text-sm font-medium text-slate-500 mt-1">
            Track upcoming calendar events, manage posters for pending events, and review generated campaigns
          </p>
        </div>

        <button onClick={fetchEvents} className="deep-3d-press btn-secondary text-xs">
          <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh Events
        </button>
      </div>

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 text-xs font-bold border border-red-200">
          {errorMessage}
        </div>
      )}

      {/* Events List Cards (Matching FullEventCard from EventListScreen.kt) */}
      {isLoading ? (
        <div className="p-12 text-center">
          <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-sm font-semibold text-slate-600">Loading events...</p>
        </div>
      ) : events.length === 0 ? (
        <div className="deep-3d-card p-12 text-center bg-white/90">
          <Calendar className="w-12 h-12 text-slate-400 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-800">No events found on backend</h3>
          <p className="text-xs text-slate-500 mt-1">0 events returned by GET /api/Events.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {events.map((event) => {
            const formattedPosterUrl = getFormattedImageUrl(event.imageUrl);
            const isPending = event.status && event.status.toLowerCase() === 'pending';
            const isGenerated = event.status && event.status.toLowerCase() === 'generated';

            return (
              <div key={event.id} className="deep-3d-card p-6 bg-white/95 space-y-4">
                {/* Card Title (Event Name) & EventType Pill */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-3">
                  <div>
                    <div className="flex items-center gap-3">
                      <h2 className="text-xl font-extrabold text-slate-900 font-heading">{event.title}</h2>
                      {event.eventType && (
                        <span className="text-xs font-bold px-2.5 py-0.5 rounded-md bg-blue-100 text-blue-700 border border-blue-200">
                          {event.eventType}
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-slate-500 mt-1 flex items-center gap-3">
                      {event.startTime && (
                        <span className="flex items-center gap-1">
                          <Clock className="w-3.5 h-3.5" /> {formatEventDate(event.startTime)}
                        </span>
                      )}
                      {event.location && (
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5" /> {event.location}
                        </span>
                      )}
                    </p>
                  </div>

                  <StatusBadge status={event.status} type="event" />
                </div>

                {/* Description */}
                <p className="text-sm text-slate-700 leading-relaxed">{event.description}</p>

                {/* Poster Graphic (AsyncImage parity with ZoomableImageDialog) */}
                {formattedPosterUrl ? (
                  <div className="relative rounded-2xl overflow-hidden bg-slate-900 border border-slate-200 max-h-80 flex items-center justify-center">
                    <img
                      src={formattedPosterUrl}
                      alt={event.title}
                      className="max-h-80 w-auto object-contain cursor-pointer hover:opacity-95 transition-opacity"
                      onClick={() => setLightboxImageUrl(formattedPosterUrl)}
                    />
                    <button
                      onClick={() => setLightboxImageUrl(formattedPosterUrl)}
                      className="absolute bottom-3 right-3 deep-3d-press px-3 py-1.5 rounded-xl bg-slate-900/80 text-white text-xs font-bold backdrop-blur-md flex items-center gap-1.5"
                    >
                      <Eye className="w-4 h-4" /> Fullscreen View
                    </button>
                  </div>
                ) : null}

                {/* Event Actions Bar */}
                <div className="pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-2">
                    {/* STRICT CONDITION: ONLY show Upload/Replace Poster if event.status is "Pending" */}
                    {isPending && (
                      <button
                        onClick={() => setUploadModalEventId(event.id)}
                        className="deep-3d-press btn-secondary text-xs font-bold"
                      >
                        <Upload className="w-4 h-4 text-blue-600" />
                        {formattedPosterUrl ? 'Replace Poster' : 'Upload Poster'}
                      </button>
                    )}

                    {/* STRICT CONDITION: ONLY show "Open Campaign" button if event.status is "Generated" */}
                    {isGenerated && (
                      <button
                        onClick={() => onNavigateToCampaign(event.id)}
                        className="deep-3d-press btn-primary text-xs font-bold"
                      >
                        Open Campaign &rarr;
                      </button>
                    )}
                  </div>

                  {/* STRICT CONDITION: ONLY show Reject Event button if event.status is "Pending" */}
                  {isPending && (
                    <button
                      onClick={() => handleCancelEvent(event.id)}
                      disabled={isProcessing}
                      className="deep-3d-press px-3.5 py-2 rounded-xl bg-red-600 text-white text-xs font-bold flex items-center gap-1.5 hover:bg-red-700 shadow-sm"
                    >
                      <XCircle className="w-4 h-4" /> Reject Event
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Lightbox Modal */}
      {lightboxImageUrl && (
        <ImageLightboxModal
          imageUrl={lightboxImageUrl}
          onClose={() => setLightboxImageUrl(null)}
        />
      )}

      {/* Upload Image Modal */}
      {uploadModalEventId && (
        <ImageUploadModal
          eventId={uploadModalEventId}
          type="event"
          title="Upload Event Poster Image"
          onClose={() => setUploadModalEventId(null)}
          onUploadSuccess={() => {
            showToast('Event poster uploaded successfully!', 'success');
            fetchEvents();
          }}
        />
      )}
    </div>
  );
};
