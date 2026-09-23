import React from 'react';
import { Volume2, Loader2 } from 'lucide-react';

interface GenerateButtonProps {
  onClick: () => void;
  isLoading: boolean;
  disabled?: boolean;
  charCount: number;
}

export const GenerateButton: React.FC<GenerateButtonProps> = ({
                                                                onClick,
                                                                isLoading,
                                                                disabled = false,
                                                                charCount,
                                                              }) => {
  return (
      <button
          id="generate-speech-btn"
          type="button"
          onClick={onClick}
          disabled={disabled || isLoading}
          className={`relative w-full sm:w-auto min-w-[200px] h-12 px-6 rounded-xl font-semibold text-white text-sm flex items-center justify-center gap-2.5 shadow-md transition-all cursor-pointer ${
              isLoading
                  ? 'bg-indigo-700 cursor-wait'
                  : disabled
                      ? 'bg-slate-300 text-slate-500 cursor-not-allowed shadow-none'
                      : 'bg-indigo-600 hover:bg-indigo-700 active:scale-[0.98] shadow-indigo-200 hover:shadow-indigo-300'
          }`}
      >
        {isLoading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin text-indigo-200" />
              <span>Generating Speech...</span>
            </>
        ) : (
            <>
              <Volume2 className="w-4 h-4" />
              <span>Generate Speech</span>
              {charCount > 0 && !disabled && (
                  <span className="hidden sm:inline-flex items-center ml-1 text-xs opacity-80 font-normal bg-indigo-700 px-2 py-0.5 rounded-full">
              {charCount} chars
            </span>
              )}
            </>
        )}
      </button>
  );
};