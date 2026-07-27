/**
 * Date formatting and expiration utility functions for Hive AI Web Application.
 */

/**
 * Formats an ISO or raw date string into a user-friendly date format (e.g., "Jul 23, 2026").
 */
export const formatEventDate = (dateStr?: string | null): string => {
  if (!dateStr) return 'TBD';
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  } catch (e) {
    return dateStr;
  }
};

/**
 * Formats a schedule timestamp into full date and time string (e.g., "Jul 23, 2026, 05:19 PM").
 */
export const formatScheduleDate = (dateStr?: string | null): string => {
  if (!dateStr) return 'Not Scheduled';
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleString('en-US', {
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

/**
 * Formats a notification timestamp matching mobile app display (e.g., "23 Jul 2026 • 05:19 PM").
 */
export const formatNotificationDate = (dateStr?: string | null): string => {
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

/**
 * Calculates the number of days remaining until an expiration date string.
 * Returns null if string is invalid or missing.
 */
export const getDaysUntilExpiration = (expiresAtStr?: string | null): number | null => {
  if (!expiresAtStr) return null;
  try {
    const expires = new Date(expiresAtStr).getTime();
    if (isNaN(expires)) return null;
    const diff = expires - Date.now();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  } catch (e) {
    return null;
  }
};
