import React, { useState, useEffect } from 'react';
import { Upload, X, AlertCircle } from 'lucide-react';
import { apiClient } from '../api/client';

interface ImageUploadModalProps {
  eventId: string;
  type: 'campaign' | 'event';
  title?: string;
  onClose: () => void;
  onUploadSuccess: (imageUrl: string) => void;
}

export const ImageUploadModal: React.FC<ImageUploadModalProps> = ({
  eventId,
  type,
  title = 'Upload Poster Image',
  onClose,
  onUploadSuccess,
}) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (!file.type.startsWith('image/')) {
        setErrorMessage('Please select a valid PNG or JPEG image file.');
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        setErrorMessage('File size exceeds the 10MB maximum limit.');
        return;
      }
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
      setErrorMessage(null);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsUploading(true);
    setErrorMessage(null);

    const formData = new FormData();
    formData.append('image', selectedFile);

    const endpoint = type === 'campaign'
      ? `/api/designer/campaigns/${eventId}/image`
      : `/api/designer/events/${eventId}/image`;

    try {
      const response = await apiClient.post(endpoint, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const uploadedUrl = response.data?.imageUrl || '';
      onUploadSuccess(uploadedUrl);
      onClose();
    } catch (err: any) {
      console.error('Image upload failed:', err);
      const msg = err.response?.data?.message || 'Failed to upload image. Please check network connection.';
      setErrorMessage(msg);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="deep-3d-card p-6 max-w-md w-full bg-white space-y-4"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b border-slate-200 pb-3">
          <h3 className="font-extrabold text-lg text-slate-900 font-heading">{title}</h3>
          <button onClick={onClose} className="p-1 rounded-lg hover:bg-slate-100 text-slate-500" title="Close (Esc)">
            <X className="w-5 h-5" />
          </button>
        </div>

        {errorMessage && (
          <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs font-bold flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        <div className="space-y-3">
          <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
            Select PNG / JPEG Image (Max 10MB)
          </label>
          <input
            type="file"
            accept="image/png, image/jpeg"
            onChange={handleFileChange}
            className="input-field text-xs cursor-pointer"
          />

          {previewUrl && (
            <div className="mt-3 relative rounded-xl overflow-hidden border border-slate-200 bg-slate-900 max-h-56 flex items-center justify-center">
              <img src={previewUrl} alt="Upload Preview" className="max-h-56 w-auto object-contain" />
            </div>
          )}
        </div>

        <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-200">
          <button onClick={onClose} className="btn-secondary text-xs">
            Cancel
          </button>
          <button
            onClick={handleUpload}
            disabled={!selectedFile || isUploading}
            className="btn-primary text-xs font-bold"
          >
            {isUploading ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <Upload className="w-4 h-4" /> Upload Image
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
