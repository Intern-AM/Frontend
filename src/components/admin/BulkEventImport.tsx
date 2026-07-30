import React, { useState, useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { 
  UploadCloud, 
  CheckCircle, 
  AlertTriangle, 
  X, 
  Loader2, 
  Eye, 
  FileSpreadsheet 
} from 'lucide-react';
import { validateBulkEvents, importBulkEvents } from '../../api/events';
import { ValidationError, BulkValidationResponse, BulkImportResponse } from '../../types';

interface BulkEventImportProps {
  onImportSuccess?: () => void;
}

export const BulkEventImport: React.FC<BulkEventImportProps> = ({ onImportSuccess }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  
  // Loading states
  const [isValidating, setIsValidating] = useState(false);
  const [isImporting, setIsImporting] = useState(false);
  
  // Response states
  const [validationResult, setValidationResult] = useState<BulkValidationResponse | null>(null);
  const [importResult, setImportResult] = useState<BulkImportResponse | null>(null);
  const [apiError, setApiError] = useState<string | null>(null);
  
  // Modal & animations
  const [showErrorsModal, setShowErrorsModal] = useState(false);
  const [modalErrors, setModalErrors] = useState<ValidationError[]>([]);
  const [showSuccessCard, setShowSuccessCard] = useState(false);

  // Error report filters
  const [activeErrorTab, setActiveErrorTab] = useState<'ALL' | 'MISSING' | 'DATE' | 'DUPLICATE' | 'OTHER'>('ALL');

  const categorizeError = (reason: string): 'MISSING' | 'DATE' | 'DUPLICATE' | 'OTHER' => {
    const lower = reason.toLowerCase();
    if (lower.includes('required') || lower.includes('missing') || lower.includes('must be provided')) {
      return 'MISSING';
    }
    if (lower.includes('date') || lower.includes('earlier') || lower.includes('time') || lower.includes('overlap')) {
      return 'DATE';
    }
    if (lower.includes('duplicate') || lower.includes('already exists') || lower.includes('same')) {
      return 'DUPLICATE';
    }
    return 'OTHER';
  };

  const missingCount = modalErrors.filter(e => categorizeError(e.reason) === 'MISSING').length;
  const dateCount = modalErrors.filter(e => categorizeError(e.reason) === 'DATE').length;
  const duplicateCount = modalErrors.filter(e => categorizeError(e.reason) === 'DUPLICATE').length;
  const otherCount = modalErrors.filter(e => categorizeError(e.reason) === 'OTHER').length;
  const totalCount = modalErrors.length;

  const filteredErrors = modalErrors.filter(error => {
    if (activeErrorTab === 'ALL') return true;
    return categorizeError(error.reason) === activeErrorTab;
  });

  // Auto-dismissal and transition timer logic for successful imports
  useEffect(() => {
    let hideTimer: NodeJS.Timeout;
    let clearTimer: NodeJS.Timeout;

    if (importResult?.success === true) {
      setShowSuccessCard(true);

      // Start fade out after 5 seconds
      hideTimer = setTimeout(() => {
        setShowSuccessCard(false);
        
        // Wait for the exit transition to complete (500ms) before clearing result data
        clearTimer = setTimeout(() => {
          setImportResult(null);
        }, 500);
      }, 5000);
    }

    return () => {
      clearTimeout(hideTimer);
      clearTimeout(clearTimer);
    };
  }, [importResult]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setValidationResult(null);
      setImportResult(null);
      setApiError(null);
    }
  };

  const handleClearFile = () => {
    setSelectedFile(null);
    setValidationResult(null);
    setImportResult(null);
    setApiError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleValidate = async () => {
    if (!selectedFile) return;
    setIsValidating(true);
    setApiError(null);
    setValidationResult(null);
    
    try {
      const res = await validateBulkEvents(selectedFile);
      setValidationResult(res);
      if (res.isValid === false && res.errors && res.errors.length > 0) {
        openErrorsModal(res.errors);
      }
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'File validation failed. Please check file format.';
      setApiError(errorMsg);
    } finally {
      setIsValidating(false);
    }
  };

  const handleImport = async () => {
    if (!selectedFile) return;
    setIsImporting(true);
    setApiError(null);
    
    try {
      const res = await importBulkEvents(selectedFile);
      setImportResult(res);
      
      if (res.success === true) {
        // Clear selection state immediately
        setSelectedFile(null);
        setValidationResult(null);
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
        // Trigger dashboard refresh callback
        if (onImportSuccess) {
          onImportSuccess();
        }
      } else if (res.isValid === false && res.errors && res.errors.length > 0) {
        openErrorsModal(res.errors);
      }
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Bulk import failed. Changes rolled back.';
      setApiError(errorMsg);
    } finally {
      setIsImporting(false);
    }
  };

  const openErrorsModal = (errors: ValidationError[]) => {
    setModalErrors(errors);
    setActiveErrorTab('ALL');
    setShowErrorsModal(true);
  };

  return (
    <div className="deep-3d-card p-6 bg-white/90 space-y-4 overflow-hidden flex flex-col justify-between">
      <div>
        <div className="border-b border-slate-100 pb-3">
          <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2 font-heading">
            <FileSpreadsheet className="w-5 h-5 text-blue-600" />
            <span>Bulk Event Import</span>
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">
            Upload an Excel file (.xlsx) containing event schedules to validate and import events in bulk.
          </p>
        </div>

        {/* Hidden file input */}
        <input
          type="file"
          ref={fileInputRef}
          accept=".xlsx"
          onChange={handleFileChange}
          className="hidden"
        />

        {/* File Selector */}
        <div className="mt-4">
          {!selectedFile ? (
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="w-full flex flex-col items-center justify-center border-2 border-dashed border-slate-300 hover:border-blue-500 rounded-2xl py-8 px-4 text-slate-500 hover:text-blue-605 transition-all cursor-pointer bg-slate-50/50 hover:bg-slate-50 group"
            >
              <UploadCloud className="h-10 w-10 text-slate-400 group-hover:text-blue-500 transition-colors mb-2" />
              <span className="text-sm font-bold text-slate-700 group-hover:text-blue-600 transition-colors mb-4">Select Excel File (.xlsx)</span>
              
              {/* Expected Spreadsheet Layout Mockup */}
              <div className="w-full max-w-lg mt-2 bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm" onClick={(e) => e.stopPropagation()}>
                {/* Column Headers */}
                <div className="grid grid-cols-5 bg-slate-50 divide-x divide-slate-200 text-center font-extrabold text-[10px] text-slate-700">
                  <div className="p-2 truncate flex items-center justify-center">Title <span className="text-red-500 ml-0.5">*</span></div>
                  <div className="p-2 truncate flex items-center justify-center">Description <span className="text-red-500 ml-0.5">*</span></div>
                  <div className="p-2 truncate flex items-center justify-center">Start Date <span className="text-red-500 ml-0.5">*</span></div>
                  <div className="p-2 truncate flex items-center justify-center">End Date <span className="text-red-500 ml-0.5">*</span></div>
                  <div className="p-2 truncate flex items-center justify-center font-heading">Event Type <span className="text-red-500 ml-0.5">*</span></div>
                </div>
              </div>
            </button>
          ) : (
            <div className="space-y-4">
              {/* Selected File Details */}
              <div className="flex items-center justify-between bg-slate-50 border border-slate-200 rounded-xl p-4">
                <div className="flex items-center space-x-3 min-w-0">
                  <div className="bg-blue-50 p-2 rounded-lg text-blue-650 border border-blue-100">
                    <FileSpreadsheet className="h-5 w-5" />
                  </div>
                  <div className="truncate">
                    <p className="text-sm font-bold text-slate-800 truncate">{selectedFile.name}</p>
                    <p className="text-[10px] text-slate-500 font-mono">{(selectedFile.size / 1024).toFixed(1)} KB</p>
                  </div>
                </div>
                <button
                  onClick={handleClearFile}
                  disabled={isValidating || isImporting}
                  className="text-slate-400 hover:text-red-600 disabled:opacity-30 p-1.5 hover:bg-slate-200/50 rounded-lg transition-colors cursor-pointer"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>

              {/* Action Control Panel */}
              <div className="flex space-x-4">
                <button
                  type="button"
                  disabled={isValidating || isImporting || validationResult !== null}
                  onClick={handleValidate}
                  className="flex-1 flex items-center justify-center gap-2 btn-primary py-2.5 px-4 justify-center text-xs font-bold disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isValidating && <Loader2 className="h-4 w-4 animate-spin" />}
                  <span>Validate File</span>
                </button>

                <button
                  type="button"
                  disabled={isImporting || isValidating || validationResult?.isValid !== true}
                  onClick={handleImport}
                  className="flex-1 flex items-center justify-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2.5 px-4 rounded-xl transition-all cursor-pointer text-xs justify-center disabled:opacity-50 disabled:cursor-not-allowed shadow-md"
                >
                  {isImporting && <Loader2 className="h-4 w-4 animate-spin" />}
                  <span>Import Events</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Bad Request or General Network Errors */}
      {apiError && (
        <div className="mt-4 flex items-start space-x-2 bg-red-50 border border-red-200 rounded-xl p-4">
          <AlertTriangle className="h-5 w-5 text-red-600 shrink-0 mt-0.5" />
          <p className="text-xs text-red-750 font-bold leading-relaxed">{apiError}</p>
        </div>
      )}

      {/* Validation Result Cards */}
      {validationResult && (
        <div className="mt-4 transition-all duration-300">
          {validationResult.isValid ? (
            <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-4 space-y-1">
              <div className="flex items-center space-x-2 text-emerald-705">
                <CheckCircle className="h-5 w-5" />
                <span className="font-extrabold text-xs uppercase tracking-wider">Validation Successful</span>
              </div>
              <p className="text-xs text-slate-700 font-medium">{validationResult.message}</p>
              <div className="text-xs text-slate-500 font-bold font-mono">
                Total Rows: {validationResult.totalRows} | Valid: {validationResult.validRows}
              </div>
            </div>
          ) : (
            <button
              type="button"
              onClick={() => openErrorsModal(validationResult.errors || [])}
              className="w-full flex items-center justify-between bg-red-50 hover:bg-red-100 border border-red-200 rounded-xl p-4 transition-colors cursor-pointer text-left shadow-sm"
            >
              <div className="flex items-center space-x-3 text-red-705">
                <AlertTriangle className="h-5 w-5 shrink-0" />
                <div>
                  <span className="font-extrabold text-xs uppercase tracking-wider block">Validation Failed</span>
                  <span className="text-xs text-slate-500 font-semibold mt-0.5 block">
                    Found {validationResult.invalidRows} invalid {validationResult.invalidRows === 1 ? 'row' : 'rows'} in spreadsheet. Click to view report.
                  </span>
                </div>
              </div>
              <span className="text-[10px] font-bold text-red-600 bg-white border border-red-200 px-2.5 py-1.5 rounded-lg shadow-sm shrink-0 flex items-center gap-1">
                <Eye className="w-3.5 h-3.5" /> View Report
              </span>
            </button>
          )}
        </div>
      )}

      {/* Import Success Notification (Animated auto-dismiss block) */}
      <div 
        className={`transition-all duration-500 ease-in-out overflow-hidden ${
          showSuccessCard 
            ? 'opacity-100 max-h-40 mt-4' 
            : 'opacity-0 max-h-0'
        }`}
      >
        {importResult?.success === true && (
          <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-4">
            <div className="flex items-center space-x-2 text-emerald-700 mb-1">
              <CheckCircle className="h-5 w-5" />
              <span className="font-extrabold text-xs uppercase tracking-wider">Import Successful</span>
            </div>
            <p className="text-xs text-slate-750 font-medium mb-1">{importResult.message}</p>
            <p className="text-xs text-slate-500 font-bold font-mono">
              Total Rows: {importResult.totalRows} | Imported: {importResult.imported}
            </p>
          </div>
        )}
      </div>

      {/* Import Failure / Re-Validation Errors */}
      {importResult && importResult.success === false && (
        <div className="mt-4 bg-red-50 border border-red-200 rounded-xl p-4">
          <div className="flex items-center space-x-2 text-red-700">
            <AlertTriangle className="h-5 w-5" />
            <span className="font-extrabold text-xs uppercase tracking-wider">Import Failed</span>
          </div>
          <p className="text-xs text-slate-750 font-medium mt-2">{importResult.message}</p>
        </div>
      )}

      {importResult && importResult.isValid === false && (
        <button
          type="button"
          onClick={() => openErrorsModal(importResult.errors || [])}
          className="mt-4 w-full flex items-center justify-between bg-red-50 hover:bg-red-105 border border-red-200 rounded-xl p-4 transition-colors cursor-pointer text-left shadow-sm"
        >
          <div className="flex items-center space-x-3 text-red-700">
            <AlertTriangle className="h-5 w-5 shrink-0" />
            <div>
              <span className="font-extrabold text-xs uppercase tracking-wider block">Import Re-Validation Failed</span>
              <span className="text-xs text-slate-500 font-semibold mt-0.5 block">
                Found {importResult.invalidRows} invalid rows. Data modified prior to submission. Click to view report.
              </span>
            </div>
          </div>
          <span className="text-[10px] font-bold text-red-600 bg-white border border-red-200 px-2.5 py-1.5 rounded-lg shadow-sm shrink-0 flex items-center gap-1 font-mono">
            <Eye className="w-3.5 h-3.5" /> View Report
          </span>
        </button>
      )}

      {/* Error Details Modal */}
      {showErrorsModal && createPortal(
        <div className="modal-overlay" onClick={() => setShowErrorsModal(false)}>
          <div 
            className="deep-3d-card p-6 max-w-2xl w-full bg-white space-y-4 shadow-2xl animate-scale-in" 
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div className="flex items-center justify-between border-b border-slate-105 pb-3">
              <h4 className="text-base font-extrabold text-slate-900 flex items-center space-x-2 font-heading">
                <AlertTriangle className="h-5 w-5 text-red-600 animate-pulse" />
                <span>Spreadsheet Validation Report</span>
              </h4>
              <button
                type="button"
                onClick={() => setShowErrorsModal(false)}
                className="text-slate-400 hover:text-slate-700 p-1.5 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {/* Stats Summary Bar */}
            <div className="grid grid-cols-3 gap-3 bg-slate-50 p-3.5 rounded-xl border border-slate-200 text-center font-mono">
              <div className="flex flex-col">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Total Rows</span>
                <span className="text-sm font-extrabold text-slate-800">
                  {validationResult?.totalRows || modalErrors.length}
                </span>
              </div>
              <div className="flex flex-col border-x border-slate-200">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Failed Rows</span>
                <span className="text-sm font-extrabold text-red-600">
                  {validationResult?.invalidRows || modalErrors.length}
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Valid Rows</span>
                <span className="text-sm font-extrabold text-emerald-600">
                  {validationResult?.validRows ?? 0}
                </span>
              </div>
            </div>

            {/* Filter Tabs */}
            <div className="flex flex-wrap gap-2 border-b border-slate-100 pb-2">
              <button
                onClick={() => setActiveErrorTab('ALL')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                  activeErrorTab === 'ALL'
                    ? 'bg-slate-900 text-white shadow-sm'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200 border border-slate-200/40'
                }`}
              >
                All ({totalCount})
              </button>
              {missingCount > 0 && (
                <button
                  onClick={() => {
                    setActiveErrorTab('MISSING');
                  }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    activeErrorTab === 'MISSING'
                      ? 'bg-orange-600 text-white shadow-sm'
                      : 'bg-orange-50 text-orange-700 hover:bg-orange-100 border border-orange-100'
                  }`}
                >
                  Missing Info ({missingCount})
                </button>
              )}
              {dateCount > 0 && (
                <button
                  onClick={() => {
                    setActiveErrorTab('DATE');
                  }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    activeErrorTab === 'DATE'
                      ? 'bg-amber-500 text-white shadow-sm'
                      : 'bg-amber-50 text-amber-800 hover:bg-amber-100 border border-amber-100'
                  }`}
                >
                  Date Conflicts ({dateCount})
                </button>
              )}
              {duplicateCount > 0 && (
                <button
                  onClick={() => {
                    setActiveErrorTab('DUPLICATE');
                  }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    activeErrorTab === 'DUPLICATE'
                      ? 'bg-purple-600 text-white shadow-sm'
                      : 'bg-purple-50 text-purple-700 hover:bg-purple-100 border border-purple-100'
                  }`}
                >
                  Duplicates ({duplicateCount})
                </button>
              )}
              {otherCount > 0 && (
                <button
                  onClick={() => {
                    setActiveErrorTab('OTHER');
                  }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    activeErrorTab === 'OTHER'
                      ? 'bg-slate-600 text-white shadow-sm'
                      : 'bg-slate-50 text-slate-700 hover:bg-slate-100 border border-slate-100'
                  }`}
                >
                  Other Errors ({otherCount})
                </button>
              )}
            </div>

            {/* Tabular Errors Table */}
            <div className="overflow-x-auto border border-slate-200 rounded-xl max-h-[300px]">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50 sticky top-0 z-10">
                  <tr className="text-left text-[10px] font-bold text-slate-500 uppercase tracking-wider font-mono">
                    <th className="px-4 py-3 text-center w-16">Row</th>
                    <th className="px-4 py-3">Event Title</th>
                    <th className="px-4 py-3 w-32">Category</th>
                    <th className="px-4 py-3">Issue Reason</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-slate-100 text-xs text-slate-700">
                  {filteredErrors.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-4 py-8 text-center text-slate-400 font-semibold">
                        No errors matching this tab category.
                      </td>
                    </tr>
                  ) : (
                    filteredErrors.map((error, idx) => {
                      const category = categorizeError(error.reason);
                      let categoryBadge = (
                        <span className="badge bg-slate-100 text-slate-700 border border-slate-200 text-[9px] py-0.5 px-1.5 font-bold uppercase">
                          Validation
                        </span>
                      );
                      
                      if (category === 'MISSING') {
                        categoryBadge = (
                          <span className="badge bg-orange-50 text-orange-700 border border-orange-200 text-[9px] py-0.5 px-1.5 font-bold uppercase">
                            Missing Info
                          </span>
                        );
                      } else if (category === 'DATE') {
                        categoryBadge = (
                          <span className="badge bg-amber-50 text-amber-800 border border-amber-200 text-[9px] py-0.5 px-1.5 font-bold uppercase">
                            Date Rule
                          </span>
                        );
                      } else if (category === 'DUPLICATE') {
                        categoryBadge = (
                          <span className="badge bg-purple-50 text-purple-700 border border-purple-200 text-[9px] py-0.5 px-1.5 font-bold uppercase">
                            Duplicate
                          </span>
                        );
                      }

                      return (
                        <tr key={idx} className="hover:bg-slate-50/60 transition-colors">
                          <td className="px-4 py-3 text-center font-bold font-mono text-red-600">
                            #{error.row}
                          </td>
                          <td className="px-4 py-3 font-semibold text-slate-800 max-w-[150px] truncate">
                            {error.title || (
                              <span className="text-slate-400 font-medium italic">Unnamed Row</span>
                            )}
                          </td>
                          <td className="px-4 py-3 whitespace-nowrap">
                            {categoryBadge}
                          </td>
                          <td className="px-4 py-3 text-slate-600 leading-normal">
                            {error.reason}
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            {/* Modal Footer */}
            <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
              <span className="text-[10px] text-slate-400 font-bold font-mono">
                Showing {filteredErrors.length} of {totalCount} errors
              </span>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
};
