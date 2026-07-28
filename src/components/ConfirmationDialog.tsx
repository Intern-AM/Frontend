import React, { useEffect } from 'react';
import { AlertCircle, HelpCircle, X } from 'lucide-react';

interface ConfirmationDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
  isProcessing?: boolean;
  type?: 'danger' | 'info' | 'success' | 'warning';
}

export const ConfirmationDialog: React.FC<ConfirmationDialogProps> = ({
  isOpen,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
  isProcessing = false,
  type = 'info',
}) => {
  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCancel();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onCancel]);

  if (!isOpen) return null;

  const getThemeClasses = () => {
    switch (type) {
      case 'danger':
        return {
          iconColor: 'text-red-600',
          bgColor: 'bg-red-50',
          btnColor: 'bg-red-600 hover:bg-red-700 shadow-red-500/20 text-white',
        };
      case 'success':
        return {
          iconColor: 'text-emerald-600',
          bgColor: 'bg-emerald-50',
          btnColor: 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-500/20 text-white',
        };
      case 'warning':
        return {
          iconColor: 'text-amber-600',
          bgColor: 'bg-amber-50',
          btnColor: 'bg-amber-600 hover:bg-amber-700 shadow-amber-500/20 text-white',
        };
      default:
        return {
          iconColor: 'text-blue-600',
          bgColor: 'bg-blue-50',
          btnColor: 'bg-blue-600 hover:bg-blue-700 shadow-blue-500/20 text-white',
        };
    }
  };

  const theme = getThemeClasses();

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div
        className="deep-3d-card p-6 max-w-md w-full bg-white space-y-4 shadow-2xl border border-slate-200"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-200 pb-3">
          <div className="flex items-center gap-2.5">
            {type === 'danger' || type === 'warning' ? (
              <AlertCircle className={`w-5 h-5 ${theme.iconColor}`} />
            ) : (
              <HelpCircle className={`w-5 h-5 ${theme.iconColor}`} />
            )}
            <h3 className="font-extrabold text-lg text-slate-900 font-heading">
              {title}
            </h3>
          </div>
          <button
            onClick={onCancel}
            disabled={isProcessing}
            className="p-1 rounded-lg hover:bg-slate-100 text-slate-500 disabled:opacity-50"
            title="Close (Esc)"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Message */}
        <div className={`p-4 rounded-xl ${theme.bgColor} border border-slate-100`}>
          <p className="text-sm font-semibold text-slate-800 leading-relaxed">
            {message}
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-200">
          <button
            type="button"
            onClick={onCancel}
            disabled={isProcessing}
            className="btn-secondary text-xs font-bold disabled:opacity-50"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isProcessing}
            className={`deep-3d-press px-4 py-2.5 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition-all ${theme.btnColor} disabled:opacity-50`}
          >
            {isProcessing ? 'Processing...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};
