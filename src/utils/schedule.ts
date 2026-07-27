import { PlatformScheduleItem } from '../types';

/**
 * Extracts platform schedule items from API schedule response data,
 * handling ASP.NET C# casing variations seamlessly.
 */
export const extractPlatformSchedules = (data: Record<string, any> = {}): PlatformScheduleItem[] => {
  const linkedInTime =
    data.schdtimeLinkedIn || data.SchdtimeLinkedIn || data.schdtimeLinkedin || data.SchdTimeLinkedIn || null;
  const instagramTime =
    data.schdtimeInstagram || data.SchdtimeInstagram || data.schdtimeinstagram || data.SchdTimeInstagram || null;
  const teamsTime =
    data.schdtimeTeams || data.SchdtimeTeams || data.schdtimeteams || data.SchdTimeTeams || null;
  const whatsappTime =
    data.schdtimeWhatsapp || data.SchdtimeWhatsapp || data.schdtimeWhatsApp || data.SchdTimeWhatsapp || null;

  const getStatusFor = (platformKey: string): string => {
    if (Array.isArray(data.platforms)) {
      const item = data.platforms.find((p: any) =>
        (p.platform || '').toLowerCase().includes(platformKey.toLowerCase())
      );
      return item?.status || 'Pending';
    }
    return 'Pending';
  };

  return [
    {
      platform: 'LinkedIn',
      scheduledTime: linkedInTime,
      status: getStatusFor('linkedin'),
    },
    {
      platform: 'Instagram',
      scheduledTime: instagramTime,
      status: getStatusFor('instagram'),
    },
    {
      platform: 'MS Teams Group',
      scheduledTime: teamsTime,
      status: getStatusFor('teams'),
    },
    {
      platform: 'WhatsApp Channel',
      scheduledTime: whatsappTime,
      status: getStatusFor('whatsapp'),
    },
  ];
};
