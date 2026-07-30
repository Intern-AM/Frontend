export interface ValidationError {
  row: number;
  title: string | null;
  reason: string;
}

export interface BulkValidationResponse {
  isValid: boolean;
  totalRows: number;
  validRows: number;
  invalidRows: number;
  message: string | null;
  errors?: ValidationError[];
}

export interface BulkImportResponse {
  success?: boolean;
  totalRows?: number;
  imported?: number;
  message?: string | null;
  isValid?: boolean;
  validRows?: number;
  invalidRows?: number;
  errors?: ValidationError[];
}
