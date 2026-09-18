import React from 'react';
import { AlertTriangle, X, RefreshCw, KeyRound, ServerOff, WifiOff } from 'lucide-react';
import { ApiError } from '../types';

interface ErrorMessageProps {
  error: ApiError | string | null;
  onDismiss: () => void;
  onRetry?: () => void;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({ error, onDismiss, onRetry }) => {
  if (!error) return null;

  const errorObj: ApiError = typeof error === 'string'
    ? { status: 400, error: 'Error', message: error }
    : error;

  const isAuth = errorObj.status === 401;
  const isServer = errorObj.status === 500 || errorObj.status === 503;
  const isNetwork = errorObj.status === 0 || errorObj.error?.toLowerCase().includes('network');

  return (
    <div
      role="alert"
      className="w-full bg-rose-50 border border-rose-200 rounded-xl p-4 shadow-xs text-rose-900 animate-in fade-in duration-200"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <div className="mt-0.5 p-1.5 rounded-lg bg-rose-100 text-rose-600">
            {isAuth ? (
              <KeyRound className="w-4 h-4" />
            ) : isNetwork ? (
              <WifiOff className="w-4 h-4" />
            ) : isServer ? (
              <ServerOff className="w-4 h-4" />
            ) : (
              <AlertTriangle className="w-4 h-4" />
            )}
          </div>

          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-2">
              <h4 className="text-sm font-semibold text-rose-950">
                {errorObj.error || 'Request Error'}
              </h4>
              {errorObj.status > 0 && (
                <span className="text-[11px] font-mono font-bold px-1.5 py-0.5 rounded bg-rose-200/80 text-rose-800">
                  HTTP {errorObj.status}
                </span>
              )}
            </div>

            <p className="text-xs text-rose-800 leading-relaxed">
              {errorObj.message}
            </p>

            {errorObj.fieldErrors && Object.keys(errorObj.fieldErrors).length > 0 && (
              <ul className="mt-1.5 space-y-0.5 text-xs text-rose-700 bg-rose-100/50 p-2 rounded-lg list-disc list-inside">
                {Object.entries(errorObj.fieldErrors).map(([field, msg]) => (
                  <li key={field}>
                    <strong className="capitalize">{field}</strong>: {msg}
                  </li>
                ))}
              </ul>
            )}

            {isAuth && (
              <p className="text-[11px] text-rose-700 mt-1 bg-white/60 p-2 rounded border border-rose-200">
                💡 <strong>Tip:</strong> In IntelliJ IDEA, set <code className="bg-rose-100 px-1 py-0.5 rounded font-mono">TTS_API_KEY</code> and <code className="bg-rose-100 px-1 py-0.5 rounded font-mono">TTS_REGION</code> in your Run Configuration Environment Variables.
              </p>
            )}
          </div>
        </div>

        <div className="flex items-center gap-1.5">
          {onRetry && (
            <button
              type="button"
              onClick={onRetry}
              className="p-1 rounded-lg hover:bg-rose-100 text-rose-700 transition-colors cursor-pointer"
              title="Retry request"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
          )}
          <button
            type="button"
            onClick={onDismiss}
            className="p-1 rounded-lg hover:bg-rose-100 text-rose-500 hover:text-rose-800 transition-colors cursor-pointer"
            title="Dismiss error"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
