import React from 'react';
import { History, Play, Trash2, Download, Calendar } from 'lucide-react';
import { SpeechHistoryItem } from '../types';
import { resolveAudioUrl } from '../services/api';

interface SpeechHistoryListProps {
    items: SpeechHistoryItem[];
    onSelect: (item: SpeechHistoryItem) => void;
    onDelete: (id: number) => void;
    isLoading?: boolean;
}

export const SpeechHistoryList: React.FC<SpeechHistoryListProps> = ({
                                                                        items,
                                                                        onSelect,
                                                                        onDelete,
                                                                    }) => {
    if (items.length === 0) {
        return (
            <div id="speech-history-empty" className="text-center py-8 px-4 rounded-xl border border-dashed border-slate-200 bg-slate-50/50">
                <History className="w-8 h-8 mx-auto text-slate-300 mb-2" />
                <p className="text-sm font-medium text-slate-600">No speech generated yet</p>
                <p className="text-xs text-slate-400 mt-0.5">
                    Previously synthesized audio history will appear here with instant replay and download options.
                </p>
            </div>
        );
    }

    return (
        <div id="speech-history-list" className="flex flex-col gap-3">
            <div className="flex items-center justify-between px-1">
                <h3 className="text-sm font-semibold text-slate-800 flex items-center gap-1.5">
                    <History className="w-4 h-4 text-indigo-600" />
                    <span>Speech History</span>
                </h3>
                <span className="text-xs font-semibold text-indigo-700 bg-indigo-50 border border-indigo-100 px-2.5 py-0.5 rounded-full">
          {items.length} {items.length === 1 ? 'record' : 'records'}
        </span>
            </div>

            <div className="space-y-2.5 max-h-[440px] overflow-y-auto pr-1">
                {items.map((item) => {
                    const dateFormatted = new Date(item.createdAt).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit',
                    });

                    return (
                        <div
                            key={item.id}
                            className="p-3.5 rounded-xl border border-slate-200/80 bg-white hover:border-indigo-300 hover:shadow-xs transition-all flex flex-col gap-2.5"
                        >
                            <div className="flex items-start justify-between gap-3">
                                <p className="text-xs text-slate-800 font-medium line-clamp-2 leading-relaxed">
                                    "{item.text}"
                                </p>
                            </div>

                            <div className="flex flex-wrap items-center justify-between gap-2 pt-2 text-[11px] text-slate-500 border-t border-slate-100">
                                <div className="flex flex-wrap items-center gap-1.5">
                  <span className="font-semibold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded text-[10px] border border-indigo-100">
                    {item.language}
                  </span>
                                    <span className="text-slate-700 font-medium">{item.voice.split('-').pop() || item.voice}</span>
                                    <span className="text-slate-300">•</span>
                                    <span>{item.characterCount} chars</span>
                                    <span className="hidden sm:inline-flex items-center gap-1 text-slate-400">
                    <Calendar className="w-3 h-3 text-slate-400" />
                                        {dateFormatted}
                  </span>
                                </div>

                                {/* Visible Action Row: Play, Download, Delete */}
                                <div className="flex items-center gap-1.5 ml-auto">
                                    <button
                                        type="button"
                                        onClick={() => onSelect(item)}
                                        className="inline-flex items-center gap-1 text-xs font-semibold text-indigo-600 hover:text-indigo-800 hover:bg-indigo-50 px-2 py-1 rounded-lg transition-colors cursor-pointer"
                                        title="Play this speech"
                                    >
                                        <Play className="w-3 h-3 fill-current" />
                                        <span>Play</span>
                                    </button>

                                    <a
                                        href={resolveAudioUrl(item.audioUrl)}
                                        download={`tts-speech-${item.id}.${item.audioFormat || 'mp3'}`}
                                        className="inline-flex items-center gap-1 text-xs font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-100 px-2 py-1 rounded-lg transition-colors cursor-pointer"
                                        title="Download audio file"
                                        aria-label="Download audio file"
                                    >
                                        <Download className="w-3.5 h-3.5" />
                                        <span>Download</span>
                                    </a>

                                    <button
                                        type="button"
                                        onClick={() => onDelete(item.id)}
                                        className="inline-flex items-center gap-1 text-xs font-semibold text-rose-600 hover:text-rose-800 hover:bg-rose-50 px-2 py-1 rounded-lg transition-colors cursor-pointer"
                                        title="Delete record from history"
                                        aria-label="Delete history item"
                                    >
                                        <Trash2 className="w-3.5 h-3.5" />
                                        <span>Delete</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};