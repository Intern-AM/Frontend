const LOCAL_POSTERS_KEY = 'hive_local_posters';

export const getLocalPosters = (): Record<string, string> => {
  try {
    const raw = localStorage.getItem(LOCAL_POSTERS_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch (e) {
    return {};
  }
};

export const saveLocalPoster = (id: string, imageUrl: string) => {
  try {
    const current = getLocalPosters();
    current[id] = imageUrl;
    localStorage.setItem(LOCAL_POSTERS_KEY, JSON.stringify(current));
  } catch (e) {
    console.warn('Failed to persist local poster:', e);
  }
};
