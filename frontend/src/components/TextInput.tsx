import React from 'react';
import { Trash2, FileText, Sparkles } from 'lucide-react';

export interface SampleText {
  label: string;
  lang: string;
  text: string;
  voice?: string;
}

export const SAMPLE_TEXTS: SampleText[] = [
  {
    label: 'English (US)',
    lang: 'en-US',
    text: 'Hello! Welcome to our text-to-speech studio.',
    voice: 'Joanna',
  },
  {
    label: 'Hindi',
    lang: 'hi-IN',
    text: 'नमस्ते! हमारे टेक्स्ट-टू-स्पीच स्टूडियो में आपका स्वागत है।',
    voice: 'Kajal',
  },
  {
    label: 'Spanish',
    lang: 'es-ES',
    text: '¡Hola! Bienvenido a nuestro estudio de texto a voz.',
    voice: 'Lucia',
  },
  {
    label: 'German',
    lang: 'de-DE',
    text: 'Hallo! Willkommen in unserem Text-to-Speech-Studio.',
    voice: 'Vicki',
  },
  {
    label: 'French',
    lang: 'fr-FR',
    text: 'Bonjour ! Bienvenue dans notre studio de synthèse vocale.',
    voice: 'Lea',
  },
];

interface TextInputProps {
  text: string;
  onChange: (val: string) => void;
  onClear: () => void;
  maxLength: number;
  disabled?: boolean;
  onSampleSelect?: (sample: SampleText | string) => void;
  onSubmit?: () => void;
}

export const TextInput: React.FC<TextInputProps> = ({
                                                      text,
                                                      onChange,
                                                      onClear,
                                                      maxLength,
                                                      disabled = false,
                                                      onSampleSelect,
                                                      onSubmit,
                                                    }) => {
  const charCount = text.length;
  const wordCount = text.trim() ? text.trim().split(/\s+/).length : 0;
  const isNearLimit = charCount > maxLength * 0.9;
  const isExceeded = charCount > maxLength;
  const progressRatio = Math.min(100, (charCount / maxLength) * 100);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      if (onSubmit && !disabled && text.trim().length > 0 && !isExceeded) {
        onSubmit();
      }
    }
  };

  return (
      <div className="flex flex-col gap-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <label htmlFor="tts-text-input" className="text-sm font-semibold text-slate-800 flex items-center gap-2">
            <FileText className="w-4 h-4 text-indigo-600" />
            <span>Text Input</span>
          </label>

          <div className="flex items-center gap-2">
            {onSampleSelect && (
                <div className="flex flex-wrap items-center gap-1.5 text-xs text-slate-500">
              <span className="hidden sm:inline-flex items-center gap-1 text-slate-400 font-medium mr-0.5">
                <Sparkles className="w-3.5 h-3.5 text-amber-500 shrink-0" />
                Samples:
              </span>
                  {SAMPLE_TEXTS.map((sample) => (
                      <button
                          key={sample.label}
                          id={`sample-btn-${sample.lang.toLowerCase()}`}
                          type="button"
                          onClick={() => onSampleSelect(sample)}
                          disabled={disabled}
                          className="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-indigo-50 hover:text-indigo-700 text-slate-700 border border-slate-200/60 hover:border-indigo-200 transition-colors text-xs font-medium cursor-pointer disabled:opacity-50 whitespace-nowrap"
                          title={`Load ${sample.label} sample (${sample.lang})`}
                      >
                        {sample.label}
                      </button>
                  ))}
                </div>
            )}

            {text.length > 0 && (
                <button
                    id="clear-text-btn"
                    type="button"
                    onClick={onClear}
                    disabled={disabled}
                    className="text-xs font-medium text-slate-500 hover:text-rose-600 flex items-center gap-1 transition-colors px-2.5 py-1 rounded-lg hover:bg-rose-50 cursor-pointer disabled:opacity-50 ml-1"
                    title="Clear text"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Clear</span>
                </button>
            )}
          </div>
        </div>

        <div className="relative">
        <textarea
            id="tts-text-input"
            value={text}
            onChange={(e) => onChange(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={disabled}
            placeholder="Type or paste text to convert to lifelike speech (Press Ctrl+Enter to generate)..."
            rows={6}
            className={`w-full p-4 rounded-xl border text-slate-800 placeholder-slate-400 bg-white text-base leading-relaxed focus:outline-none transition-all resize-y shadow-xs ${
                isExceeded
                    ? 'border-rose-500 focus:ring-2 focus:ring-rose-200'
                    : 'border-slate-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100'
            } ${disabled ? 'bg-slate-50 cursor-not-allowed opacity-75' : ''}`}
        />
          {/* Subtle progress indicator bar */}
          <div className="absolute bottom-1 left-3 right-3 h-1 bg-slate-100 rounded-full overflow-hidden">
            <div
                className={`h-full transition-all duration-150 ${
                    isExceeded
                        ? 'bg-rose-500'
                        : isNearLimit
                            ? 'bg-amber-500'
                            : 'bg-indigo-500'
                }`}
                style={{ width: `${progressRatio}%` }}
            />
          </div>
        </div>

        <div className="flex flex-wrap items-center justify-between text-xs text-slate-500 px-1 gap-2">
          <div className="flex items-center gap-4">
          <span>
            Words: <strong className="text-slate-700 font-semibold">{wordCount}</strong>
          </span>
            <span>
            Reading Time: <strong className="text-slate-700 font-semibold">~{Math.max(1, Math.round(wordCount / 2.5))}s</strong>
          </span>
          </div>

          <div className="flex items-center gap-2">
          <span className={isExceeded ? 'text-rose-600 font-bold' : isNearLimit ? 'text-amber-600 font-medium' : 'text-slate-600'}>
            {charCount.toLocaleString()} / {maxLength.toLocaleString()} characters
          </span>
          </div>
        </div>

        {isExceeded && (
            <p className="text-xs text-rose-600 font-medium bg-rose-50 p-2.5 rounded-lg border border-rose-200">
              Text exceeds the maximum allowed limit of {maxLength} characters by {charCount - maxLength} characters.
            </p>
        )}
      </div>
  );
};
