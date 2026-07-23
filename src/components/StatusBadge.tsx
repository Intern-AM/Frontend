import React from 'react';
import { CheckCircle2, Clock, XCircle, AlertCircle, Shield, Eye, Send } from 'lucide-react';
import { CampaignStatus, PostingStatus, UserRole } from '../types';

interface StatusBadgeProps {
  status?: CampaignStatus | PostingStatus | UserRole | string;
  type?: 'campaign' | 'posting' | 'role' | 'event';
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status = '', type = 'campaign' }) => {
  const lowerStatus = status.toString().toLowerCase();

  if (type === 'role') {
    if (lowerStatus === 'admin') {
      return (
        <span className="badge badge-admin">
          <Shield className="w-3.5 h-3.5" /> Admin
        </span>
      );
    }
    return (
      <span className="badge badge-reviewer">
        <Eye className="w-3.5 h-3.5" /> Reviewer
      </span>
    );
  }

  if (lowerStatus === 'approved' || lowerStatus === 'posted' || lowerStatus === 'synced' || lowerStatus === 'active') {
    return (
      <span className="badge badge-approved">
        <CheckCircle2 className="w-3.5 h-3.5" /> {status}
      </span>
    );
  }

  if (lowerStatus === 'pending' || lowerStatus === 'generated') {
    return (
      <span className="badge badge-pending">
        <Clock className="w-3.5 h-3.5" /> {status}
      </span>
    );
  }

  if (lowerStatus === 'rejected' || lowerStatus === 'failed' || lowerStatus === 'cancelled') {
    return (
      <span className="badge badge-failed">
        <XCircle className="w-3.5 h-3.5" /> {status}
      </span>
    );
  }

  if (lowerStatus === 'published') {
    return (
      <span className="badge badge-approved">
        <Send className="w-3.5 h-3.5" /> Published
      </span>
    );
  }

  return (
    <span className="badge badge-pending">
      <AlertCircle className="w-3.5 h-3.5" /> {status}
    </span>
  );
};
