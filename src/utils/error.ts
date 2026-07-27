import axios from 'axios';

/**
 * Extracts a human-readable error message from an unknown error or Axios error object.
 *
 * @param err The error caught in try/catch
 * @param fallbackMessage Default message to return if no specific message is extracted
 */
export const getErrorMessage = (err: unknown, fallbackMessage: string): string => {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message || err.message || fallbackMessage;
  }
  if (err instanceof Error) {
    return err.message || fallbackMessage;
  }
  if (typeof err === 'string') {
    return err;
  }
  return fallbackMessage;
};
