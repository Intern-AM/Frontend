import React, { useState } from 'react';
import { X, ZoomIn, ZoomOut, RotateCw } from 'lucide-react';

interface ImageLightboxModalProps {
  imageUrl: string;
  title?: string;
  onClose: () => void;
}

export const ImageLightboxModal: React.FC<ImageLightboxModalProps> = ({
  imageUrl,
  title = 'Media Preview',
  onClose,
}) => {
  const [scale, setScale] = useState(1);
  const [rotation, setRotation] = useState(0);

  const handleZoomIn = () => setScale((prev) => Math.min(prev + 0.3, 3));
  const handleZoomOut = () => setScale((prev) => Math.max(prev - 0.3, 0.5));
  const handleRotate = () => setRotation((prev) => (prev + 90) % 360);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="deep-3d-card relative max-w-4xl w-full p-4 flex flex-col items-center max-h-[90vh] bg-white/95"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Header */}
        <div className="w-full flex items-center justify-between border-b border-slate-200 pb-3 mb-3">
          <h3 className="text-lg font-bold text-slate-900">{title}</h3>
          <div className="flex items-center gap-2">
            <button
              onClick={handleZoomIn}
              className="deep-3d-press p-2 rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200"
              title="Zoom In"
            >
              <ZoomIn className="w-4 h-4" />
            </button>
            <button
              onClick={handleZoomOut}
              className="deep-3d-press p-2 rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200"
              title="Zoom Out"
            >
              <ZoomOut className="w-4 h-4" />
            </button>
            <button
              onClick={handleRotate}
              className="deep-3d-press p-2 rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200"
              title="Rotate"
            >
              <RotateCw className="w-4 h-4" />
            </button>
            <button
              onClick={onClose}
              className="deep-3d-press p-2 rounded-lg bg-red-100 text-red-600 hover:bg-red-200 ml-2"
              title="Close"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Media Container */}
        <div className="w-full flex-1 overflow-hidden flex items-center justify-center min-h-[400px] bg-slate-950/90 rounded-xl p-4 border border-slate-800">
          <img
            src={imageUrl}
            alt={title}
            className="max-h-[70vh] max-w-full object-contain transition-transform duration-200"
            style={{
              transform: `scale(${scale}) rotate(${rotation}deg)`,
            }}
          />
        </div>
      </div>
    </div>
  );
};
