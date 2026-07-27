/**
 * Role assigned to users within the Hive AI system.
 */
export type UserRole = 'Admin' | 'Reviewer';

/**
 * Status of campaigns throughout the approval and publishing lifecycle.
 */
export type CampaignStatus = 'Active' | 'Generated' | 'Approved' | 'Rejected' | 'Published' | string;

/**
 * Status of per-platform social media postings.
 */
export type PostingStatus = 'Pending' | 'Posted' | 'Failed' | 'Scheduled' | string;

/**
 * Categories of system notifications.
 */
export type NotificationType = 'REVIEW_REQUIRED' | 'APPROVED' | 'REJECTED' | 'PUBLISHED' | 'EVENT_CANCELLED';

/**
 * User account model.
 */
export interface User {
  id: string;
  name?: string;
  email?: string;
  username: string;
  role: UserRole;
  isActive: boolean;
  createdAt?: string;
}

/**
 * Marketing campaign model created for an event.
 */
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

/**
 * Event model synced with the calendar.
 */
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
  imageUrl?: string | null;
}

/**
 * Per-platform social media posting record.
 */
export interface PlatformPosting {
  platform: string;
  status: PostingStatus;
  createdAt?: string | null;
  postedAt?: string | null;
  errorMessage?: string | null;
  failureReason?: string;
}

/**
 * Schedule item for an individual social platform.
 */
export interface PlatformScheduleItem {
  platform: string;
  scheduledTime?: string | null;
  status?: PostingStatus;
}

/**
 * Response contract for campaign schedule retrieval.
 */
export interface CampaignScheduleResponse {
  eventId: string;
  schedules: PlatformScheduleItem[];
}

/**
 * Compliance audit log item.
 */
export interface AuditLog {
  id: string;
  userId: string;
  username?: string;
  action: string;
  details: string;
  createdAt?: string;
  timestamp?: string;
}

/**
 * Admin configuration model for social media platform credentials & API tokens.
 */
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

/**
 * System notification item presented in Notification Center.
 */
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

