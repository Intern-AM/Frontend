import { apiClient as api } from './client';
import { BulkValidationResponse, BulkImportResponse } from '../types';

/**
 * Validates the Excel file content prior to import.
 */
export const validateBulkEvents = async (file: File): Promise<BulkValidationResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post<BulkValidationResponse>('/api/events/import/validate', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};

/**
 * Executes the bulk events import.
 */
export const importBulkEvents = async (file: File): Promise<BulkImportResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post<BulkImportResponse>('/api/events/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};
