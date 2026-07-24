import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw, Home } from 'lucide-react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
    errorInfo: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error, errorInfo: null };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Unhandled React Error Boundary caught an exception:', error, errorInfo);
    this.setState({ errorInfo });
  }

  private handleReset = () => {
    this.setState({ hasError: false, error: null, errorInfo: null });
    window.location.href = '/';
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen w-full flex items-center justify-center p-6 bg-gradient-to-br from-slate-100 via-red-50/30 to-slate-200">
          <div className="max-w-lg w-full deep-3d-card p-8 bg-white/95 text-center space-y-6 border border-red-200 shadow-2xl">
            <div className="w-16 h-16 rounded-2xl bg-red-100 text-red-600 flex items-center justify-center mx-auto shadow-md">
              <AlertTriangle className="w-9 h-9" />
            </div>

            <div>
              <h1 className="text-2xl font-extrabold text-slate-900 font-heading">
                Something went wrong
              </h1>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                An unexpected error occurred while rendering this view. Your session data remains safe.
              </p>
            </div>

            {this.state.error && (
              <div className="p-4 rounded-xl bg-slate-900 text-slate-200 text-left font-mono text-xs overflow-x-auto max-h-36 border border-slate-700">
                <p className="text-red-400 font-bold">{this.state.error.toString()}</p>
                {this.state.errorInfo && (
                  <pre className="text-[11px] text-slate-400 mt-2 whitespace-pre-wrap">
                    {this.state.errorInfo.componentStack}
                  </pre>
                )}
              </div>
            )}

            <div className="flex items-center justify-center gap-3 pt-2">
              <button
                onClick={() => window.location.reload()}
                className="deep-3d-press btn-secondary text-xs font-bold flex items-center gap-2"
              >
                <RefreshCw className="w-4 h-4" />
                Reload Page
              </button>
              <button
                onClick={this.handleReset}
                className="deep-3d-press btn-primary text-xs font-bold flex items-center gap-2 shadow-md shadow-blue-500/25"
              >
                <Home className="w-4 h-4" />
                Return to Dashboard
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
