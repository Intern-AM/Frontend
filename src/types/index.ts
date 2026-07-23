export type UserRole = 'Admin' | 'Reviewer' | 'Designer';

export type CampaignStatus = 'Active' | 'Generated' | 'Approved' | 'Rejected' | 'Published' | string;

export type PostingStatus = 'Pending' | 'Posted' | 'Failed' | 'Scheduled' | string;

export type NotificationType = 'REVIEW_REQUIRED' | 'APPROVED' | 'REJECTED' | 'PUBLISHED' | 'EVENT_CANCELLED';

export interface User {
  id: string;
  name?: string;
  email?: string;
  username: string;
  role: UserRole;
  isActive: boolean;
  createdAt?: string;
}

export interface Campaign {
  campaignId: number;
  eventId: string;
  eventTitle?: string;
  campaignPost: string;
  hashtags: string;
  cta: string;
  imagePrompt: string;
  imageUrl?: string | null;
  status: CampaignStatus;
  createdAt: string;
  linkedInPostId?: string | null;
  postedAt?: string | null;
}

export interface SpeehiveEvent {
  id: string;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  location: string;
  eventType: string;
  status: string;
  approvalDeadline?: string | null;
  designerImageUrl?: string | null;
}

export interface PlatformPosting {
  platform: string;
  status: PostingStatus;
  createdAt?: string | null;
  postedAt?: string | null;
  errorMessage?: string | null;
  failureReason?: string;
}

export interface PlatformScheduleItem {
  platform: string;
  scheduledTime?: string | null;
  status?: PostingStatus;
}

export interface CampaignScheduleResponse {
  eventId: string;
  schedules: PlatformScheduleItem[];
}

export interface AuditLog {
  id: string;
  userId: string;
  username?: string;
  action: string;
  details: string;
  createdAt?: string;
  timestamp?: string;
}

export interface SocialMediaCredential {
  id?: string;
  provider: string;
  maskedToken?: string;
  expiresAt?: string | null;
  isActive?: boolean;
  updatedAt?: string | null;
  accountName?: string;
  platform?: string;
  expiryDate?: string;
  isValid?: boolean;
}

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  timestamp: string;
  type: NotificationType;
  isRead: boolean;
  eventId?: string;
  campaignId?: string;
  platformPostings?: PlatformPosting[];
}
