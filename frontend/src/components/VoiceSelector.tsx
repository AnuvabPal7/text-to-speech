import React from 'react';
import { Mic } from 'lucide-react';
import { Voice } from '../types';

interface VoiceSelectorProps {
  voices: Voice[];
  selectedVoice: string;
  onChange: (voiceId: string) => void;
  disabled?: boolean;
}

export const VoiceSelector: React.FC<VoiceSelectorProps> = ({
  voices,
  selectedVoice,
  onChange,
  disabled = false,
}) => {
  const currentVoice = voices.find((v) => v.id === selectedVoice) || voices[0];

  return (
    <div className="flex flex-col gap-1.5 flex-1">
      <div className="flex items-center justify-between">
        <label htmlFor="voice-select" className="text-sm font-semibold text-slate-800 flex items-center gap-1.5">
          <Mic className="w-4 h-4 text-indigo-600" />
          <span>Voice Model</span>
        </label>
        <span className="text-xs text-slate-500 font-medium">
          {voices.length} {voices.length === 1 ? 'voice' : 'voices'} available
        </span>
      </div>

      <div className="relative">
        <select
          id="voice-select"
          value={selectedVoice}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled || voices.length === 0}
          className="w-full h-11 pl-3.5 pr-10 rounded-xl border border-slate-200 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 transition-all shadow-xs appearance-none cursor-pointer disabled:bg-slate-50 disabled:cursor-not-allowed"
        >
          {voices.map((voice) => (
            <option key={voice.id} value={voice.id}>
              {voice.name} [{voice.gender}] {voice.accent ? `— ${voice.accent}` : ''}
            </option>
          ))}
        </select>

        <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3.5 text-slate-400">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </div>

      {currentVoice && (
        <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500 px-1">
          <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 font-medium">
            {currentVoice.gender}
          </span>
          {currentVoice.accent && (
            <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded bg-indigo-50 text-indigo-700 font-medium">
              {currentVoice.accent}
            </span>
          )}
          {currentVoice.engine && (
            <span className={`inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] font-semibold border ${
              currentVoice.engine === 'neural'
                ? 'bg-purple-50 text-purple-700 border-purple-200/70'
                : 'bg-amber-50 text-amber-700 border-amber-200/70'
            }`}>
              Engine: {currentVoice.engine.toUpperCase()}
            </span>
          )}
          <span className="text-slate-400">ID: {currentVoice.id}</span>
        </div>
      )}
    </div>
  );
};
