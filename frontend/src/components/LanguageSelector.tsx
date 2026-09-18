import React from 'react';
import { Globe } from 'lucide-react';
import { Language } from '../types';

interface LanguageSelectorProps {
  languages: Language[];
  selectedLanguage: string;
  onChange: (code: string) => void;
  disabled?: boolean;
}

export const LanguageSelector: React.FC<LanguageSelectorProps> = ({
  languages,
  selectedLanguage,
  onChange,
  disabled = false,
}) => {
  const currentLang = languages.find((l) => l.code === selectedLanguage);

  return (
    <div className="flex flex-col gap-1.5 flex-1">
      <label htmlFor="language-select" className="text-sm font-semibold text-slate-800 flex items-center gap-1.5">
        <Globe className="w-4 h-4 text-indigo-600" />
        <span>Language</span>
      </label>

      <div className="relative">
        <select
          id="language-select"
          value={selectedLanguage}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
          className="w-full h-11 pl-3.5 pr-10 rounded-xl border border-slate-200 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 transition-all shadow-xs appearance-none cursor-pointer disabled:bg-slate-50 disabled:cursor-not-allowed"
        >
          {languages.map((lang) => (
            <option key={lang.code} value={lang.code}>
              {lang.flag} {lang.name} ({lang.nativeName})
            </option>
          ))}
        </select>

        <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3.5 text-slate-400">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </div>

      {currentLang && (
        <span className="text-xs text-slate-500 px-1">
          Active: <span className="font-medium text-slate-700">{currentLang.nativeName}</span> ({currentLang.code})
        </span>
      )}
    </div>
  );
};
