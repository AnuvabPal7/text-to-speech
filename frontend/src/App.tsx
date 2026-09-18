import { useState, useEffect, useCallback } from 'react';
import {
  Volume2,
  Sliders,
  RotateCcw,
  CheckCircle2,
  Server,
  Database,
  RefreshCw,
  X,
  Cpu,
} from 'lucide-react';
import { TextInput, SAMPLE_TEXTS, SampleText } from './components/TextInput';
import { LanguageSelector } from './components/LanguageSelector';
import { VoiceSelector } from './components/VoiceSelector';
import { GenerateButton } from './components/GenerateButton';
import { AudioPlayer } from './components/AudioPlayer';
import { ErrorMessage } from './components/ErrorMessage';
import { SpeechHistoryList } from './components/SpeechHistoryList';
import {
  DEFAULT_LANGUAGES,
  fetchVoices,
  generateSpeech,
  fetchHealth,
  fetchHistory,
  deleteHistory,
} from './services/api';
import { Voice, TtsResponse, HealthStatus, BackendStatus, SpeechHistoryItem, ApiError } from './types';

const MAX_CHAR_LIMIT = 5000;
const HEALTH_POLL_INTERVAL_MS = 10000; // 10s live polling of backend health

const BACKEND_STATUS_DISPLAY: Record<BackendStatus, { label: string; badgeClass: string; dotClass: string }> = {
  CHECKING: {
    label: 'CHECKING...',
    badgeClass: 'bg-slate-100 text-slate-600 border-slate-200',
    dotClass: 'bg-slate-400 animate-pulse',
  },
  UP: {
    label: 'UP',
    badgeClass: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    dotClass: 'bg-emerald-500 animate-pulse',
  },
  DEGRADED: {
    label: 'DEGRADED',
    badgeClass: 'bg-amber-50 text-amber-700 border-amber-200',
    dotClass: 'bg-amber-500 animate-pulse',
  },
  DOWN: {
    label: 'DOWN',
    badgeClass: 'bg-red-50 text-red-700 border-red-200',
    dotClass: 'bg-red-500',
  },
};

export default function App() {
  const [text, setText] = useState<string>(
      'Hello! Welcome to our text-to-speech studio.'
  );
  const [language, setLanguage] = useState<string>('en-US');
  const [voice, setVoice] = useState<string>('Joanna');
  const [speed, setSpeed] = useState<number>(1.0);
  const [pitch, setPitch] = useState<number>(1.0);
  const [showAdvanced, setShowAdvanced] = useState<boolean>(false);

  const [availableVoices, setAvailableVoices] = useState<Voice[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isRefreshingHealth, setIsRefreshingHealth] = useState<boolean>(false);
  const [currentAudio, setCurrentAudio] = useState<TtsResponse | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [health, setHealth] = useState<HealthStatus | null>(null);
  // Real connectivity state, driven only by an actual HTTP response from the
  // Spring Boot /api/health endpoint. Starts CHECKING - never defaults to UP.
  const [backendStatus, setBackendStatus] = useState<BackendStatus>('CHECKING');
  const [history, setHistory] = useState<SpeechHistoryItem[]>([]);
  const [isDeletingAudio, setIsDeletingAudio] = useState<boolean>(false);

  // Actually hits GET /api/health on the Spring Boot backend (proxied from
  // Vite's :3000 to :8080). Only a genuine successful HTTP response sets UP;
  // any failure - connection refused, network error, timeout - sets DOWN.
  // This never reports UP just because the React/Vite dev server is running.
  const checkBackendHealth = useCallback(async () => {
    try {
      const h = await fetchHealth();
      setHealth(h);
      setBackendStatus(h.status);
    } catch {
      setHealth(null);
      setBackendStatus('DOWN');
    }
  }, []);

  // Load voices and history. Health is checked separately (see below) so a
  // backend outage never prevents the fallback voices/history from loading.
  const loadInitialData = useCallback(async () => {
    try {
      const [v, hist] = await Promise.all([
        fetchVoices(language),
        fetchHistory(20),
      ]);
      setAvailableVoices(v);
      if (v.length > 0 && !v.some((item) => item.id === voice)) {
        setVoice(v[0].id);
      }
      setHistory(hist);
    } catch (err) {
      console.warn('Initial data load notice:', err);
    }
  }, [language, voice]);

  // Preserves original behavior: voices/history reload whenever language or
  // voice changes (unrelated to the health check, left untouched).
  useEffect(() => {
    loadInitialData();
  }, [loadInitialData]);

  // Health check runs once on mount (starts as CHECKING, never defaults to
  // UP), then on a fixed interval - independent of language/voice - so it
  // keeps polling the real backend without being tied to unrelated state.
  useEffect(() => {
    checkBackendHealth();

    const intervalId = setInterval(() => {
      checkBackendHealth();
    }, HEALTH_POLL_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [checkBackendHealth]);

  // Refresh health manually
  const handleRefreshHealth = async () => {
    setIsRefreshingHealth(true);
    try {
      await Promise.all([
        checkBackendHealth(),
        fetchHistory(20).then(setHistory).catch(() => {}),
      ]);
    } finally {
      setIsRefreshingHealth(false);
    }
  };

  // Update voices whenever language changes
  const handleLanguageChange = async (newLang: string) => {
    setLanguage(newLang);
    setError(null);
    try {
      const voicesForLang = await fetchVoices(newLang);
      setAvailableVoices(voicesForLang);
      if (voicesForLang.length > 0) {
        // Automatically select the first valid voice for the new language
        setVoice(voicesForLang[0].id);
      } else {
        setVoice('');
      }
    } catch {
      // Fallback in API service handles it
    }
  };

  // Sample prompt selection: updates text, language, and auto-selects valid AWS Polly voice
  const handleSampleSelect = async (sampleInput: SampleText | string) => {
    const selected = typeof sampleInput === 'string'
        ? SAMPLE_TEXTS.find((s) => s.text === sampleInput || s.label === sampleInput)
        : sampleInput;

    const sampleText = typeof sampleInput === 'string' ? sampleInput : sampleInput.text;
    setText(sampleText);
    setError(null);

    if (selected) {
      const targetLang = selected.lang;
      setLanguage(targetLang);
      try {
        const voicesForLang = await fetchVoices(targetLang);
        setAvailableVoices(voicesForLang);
        if (voicesForLang.length > 0) {
          // If sample specifies a valid voice in this language, select it
          const matchingVoice = selected.voice
              ? voicesForLang.find((v) => v.id.toLowerCase() === selected.voice!.toLowerCase())
              : null;

          if (matchingVoice) {
            setVoice(matchingVoice.id);
          } else {
            setVoice(voicesForLang[0].id);
          }
        } else {
          setVoice('');
        }
      } catch (err) {
        console.warn('Failed to load voices for sample language:', err);
      }
    }
  };

  // Generate Speech handler
  const handleGenerate = async () => {
    const trimmed = text.trim();
    if (!trimmed) {
      setError({
        status: 400,
        error: 'Validation Error',
        message: 'Text cannot be empty or whitespace.',
      });
      return;
    }

    if (trimmed.length > MAX_CHAR_LIMIT) {
      setError({
        status: 400,
        error: 'Text Limit Exceeded',
        message: `Text exceeds the maximum allowed limit of ${MAX_CHAR_LIMIT} characters. Currently ${trimmed.length} characters.`,
      });
      return;
    }

    // Determine the matched voice and engine
    const matchedVoice = availableVoices.find((v) => v.id === voice) || availableVoices[0];
    const resolvedEngine = matchedVoice?.engine || (voice.toLowerCase() === 'kajal' ? 'neural' : voice.toLowerCase() === 'aditi' ? 'standard' : 'neural');

    setIsLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const response = await generateSpeech({
        text: trimmed,
        language,
        voice,
        engine: resolvedEngine,
        speed,
        pitch,
      });

      setCurrentAudio(response);
      setSuccessMessage(
          `Speech synthesized successfully (${trimmed.length} characters) using voice "${voice}" with ${resolvedEngine.toUpperCase()} engine.`
      );

      // Refresh speech history from PostgreSQL
      const updatedHist = await fetchHistory(20);
      setHistory(updatedHist);
    } catch (err: unknown) {
      const apiErr = err as ApiError;
      setError(apiErr);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteHistory = async (id: number) => {
    try {
      const ok = await deleteHistory(id);
      if (ok) {
        setHistory((prev) => prev.filter((item) => item.id !== id));
        if (currentAudio && (currentAudio.historyId === id || history.find((h) => h.id === id)?.audioUrl === currentAudio.audioUrl)) {
          setCurrentAudio(null);
        }
      } else {
        setError({
          status: 500,
          error: 'Delete Error',
          message: 'Failed to delete speech record from backend.',
        });
      }
    } catch {
      setError({
        status: 500,
        error: 'Delete Error',
        message: 'An unexpected error occurred while deleting the audio record.',
      });
    }
  };

  // Delete currently loaded audio from player and backend history
  const handleDeleteCurrentAudio = async () => {
    if (!currentAudio) return;

    setIsDeletingAudio(true);
    setError(null);

    try {
      // If historyId is known on the audio object, or lookup by audioUrl in history records
      const targetId = currentAudio.historyId || history.find((item) => item.audioUrl === currentAudio.audioUrl)?.id;

      if (targetId) {
        const ok = await deleteHistory(targetId);
        if (!ok) {
          throw new Error('Failed to delete history item on server');
        }
        setHistory((prev) => prev.filter((item) => item.id !== targetId));
      }

      // Remove audio player from the screen
      setCurrentAudio(null);
      setSuccessMessage('Generated speech audio was successfully deleted.');
    } catch (err) {
      setError({
        status: 500,
        error: 'Deletion Failed',
        message: 'Could not delete the generated audio. Please try again.',
      });
    } finally {
      setIsDeletingAudio(false);
    }
  };

  const handleSelectHistoryItem = (item: SpeechHistoryItem) => {
    setCurrentAudio({
      success: true,
      audioUrl: item.audioUrl,
      format: item.audioFormat,
      characterCount: item.characterCount,
      wordCount: item.wordCount,
      historyId: item.id,
      message: 'Loaded from history',
    });
    setLanguage(item.language);
    setVoice(item.voice);
    setText(item.text);
    setSuccessMessage(`Loaded speech from history record #${item.id}.`);
  };

  const resetAudioSettings = () => {
    setSpeed(1.0);
    setPitch(1.0);
  };

  const currentVoiceObj = availableVoices.find((v) => v.id === voice);
  const activeEngine = currentVoiceObj?.engine || (voice.toLowerCase() === 'kajal' ? 'neural' : voice.toLowerCase() === 'aditi' ? 'standard' : 'neural');

  return (
      <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased selection:bg-indigo-500 selection:text-white">
        {/* Top Navigation Bar */}
        <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md border-b border-slate-200 shadow-xs">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-indigo-600 text-white flex items-center justify-center shadow-xs shadow-indigo-200 shrink-0">
                <Volume2 className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h1 className="text-base font-bold text-slate-900 tracking-tight">
                    Text-to-Speech Studio
                  </h1>
                  <span className="hidden sm:inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200/60">
                  AWS Amazon Polly
                </span>
                </div>
                <p className="text-xs text-slate-500 hidden md:block">
                  Convert written text into natural, lifelike speech with multi-language neural voices
                </p>
              </div>
            </div>

            {/* Operational Status Badges */}
            <div className="flex items-center gap-2 sm:gap-3">
              <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 border border-slate-200/80 text-xs text-slate-700 font-medium">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse shrink-0" />
                <span className="hidden sm:inline text-slate-500 font-normal">Provider:</span>
                <span className="font-semibold text-slate-800">
                {health?.provider ? 'AWS Polly' : 'AWS Polly (Active)'}
              </span>
              </div>

              <div className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 border border-slate-200/80 text-xs text-slate-700 font-medium">
                <Database className="w-3.5 h-3.5 text-indigo-600 shrink-0" />
                <span className="text-slate-500 font-normal">Database:</span>
                <span className="font-semibold text-slate-800">
                {health?.databaseStatus === 'CONNECTED' ? 'Connected' : 'PostgreSQL'}
              </span>
              </div>
            </div>
          </div>
        </header>

        {/* Main Workspace Layout */}
        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 py-6 sm:py-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-start">
            {/* Left / Center Column: Text-to-Speech Synthesizer (7 or 8 cols on desktop) */}
            <div className="lg:col-span-8 flex flex-col gap-6">
              <div className="bg-white rounded-2xl border border-slate-200/90 p-5 sm:p-6 shadow-xs flex flex-col gap-5">
                {/* Text Input with sample buttons, word count, reading time */}
                <TextInput
                    text={text}
                    onChange={setText}
                    onClear={() => {
                      setText('');
                      setSuccessMessage(null);
                    }}
                    maxLength={MAX_CHAR_LIMIT}
                    disabled={isLoading}
                    onSampleSelect={handleSampleSelect}
                    onSubmit={handleGenerate}
                />

                {/* Language & Voice Selector Controls */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-slate-100">
                  <LanguageSelector
                      languages={DEFAULT_LANGUAGES}
                      selectedLanguage={language}
                      onChange={handleLanguageChange}
                      disabled={isLoading}
                  />

                  <VoiceSelector
                      voices={availableVoices}
                      selectedVoice={voice}
                      onChange={setVoice}
                      disabled={isLoading}
                  />
                </div>

                {/* Speaking Speed & Pitch Customization */}
                <div className="pt-2 border-t border-slate-100">
                  <div className="flex items-center justify-between">
                    <button
                        type="button"
                        onClick={() => setShowAdvanced(!showAdvanced)}
                        className="text-xs font-semibold text-slate-600 hover:text-indigo-600 flex items-center gap-1.5 transition-colors cursor-pointer py-1"
                    >
                      <Sliders className="w-3.5 h-3.5 text-indigo-600" />
                      <span>{showAdvanced ? 'Hide' : 'Show'} Voice Settings (Speed &amp; Pitch)</span>
                    </button>

                    {(speed !== 1.0 || pitch !== 1.0) && (
                        <button
                            type="button"
                            onClick={resetAudioSettings}
                            className="text-xs text-slate-500 hover:text-indigo-600 flex items-center gap-1 transition-colors cursor-pointer"
                        >
                          <RotateCcw className="w-3 h-3" />
                          <span>Reset sliders</span>
                        </button>
                    )}
                  </div>

                  {showAdvanced && (
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mt-3 p-4 rounded-xl bg-slate-50/80 border border-slate-200/80 text-xs animate-in fade-in duration-150">
                        <div className="flex flex-col gap-2">
                          <div className="flex items-center justify-between">
                            <span className="font-semibold text-slate-700">Speaking Rate</span>
                            <span className="font-mono text-indigo-600 font-semibold px-2 py-0.5 rounded bg-indigo-50 border border-indigo-100">
                          {speed.toFixed(2)}x
                        </span>
                          </div>
                          <input
                              id="speed-slider"
                              type="range"
                              min="0.5"
                              max="2.0"
                              step="0.05"
                              value={speed}
                              onChange={(e) => setSpeed(parseFloat(e.target.value))}
                              disabled={isLoading}
                              className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-indigo-600 focus:outline-none"
                          />
                          <div className="flex justify-between text-[11px] text-slate-400">
                            <span>0.5x (Slow)</span>
                            <span>1.0x (Normal)</span>
                            <span>2.0x (Fast)</span>
                          </div>
                        </div>

                        <div className="flex flex-col gap-2">
                          <div className="flex items-center justify-between">
                            <span className="font-semibold text-slate-700">Voice Pitch</span>
                            <span className="font-mono text-indigo-600 font-semibold px-2 py-0.5 rounded bg-indigo-50 border border-indigo-100">
                          {pitch.toFixed(2)}x
                        </span>
                          </div>
                          <input
                              id="pitch-slider"
                              type="range"
                              min="0.5"
                              max="1.5"
                              step="0.05"
                              value={pitch}
                              onChange={(e) => setPitch(parseFloat(e.target.value))}
                              disabled={isLoading}
                              className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-indigo-600 focus:outline-none"
                          />
                          <div className="flex justify-between text-[11px] text-slate-400">
                            <span>0.5x (Deep)</span>
                            <span>1.0x (Default)</span>
                            <span>1.5x (High)</span>
                          </div>
                        </div>
                      </div>
                  )}
                </div>

                {/* Error Message */}
                {error && (
                    <ErrorMessage
                        error={error}
                        onDismiss={() => setError(null)}
                        onRetry={handleGenerate}
                    />
                )}

                {/* Success Message Banner */}
                {successMessage && !error && (
                    <div
                        id="success-banner"
                        role="status"
                        className="flex items-start justify-between gap-2 p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-900 text-xs font-medium animate-in fade-in duration-200"
                    >
                      <div className="flex items-center gap-2">
                        <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                        <span>{successMessage}</span>
                      </div>
                      <button
                          type="button"
                          onClick={() => setSuccessMessage(null)}
                          className="text-emerald-700 hover:text-emerald-900 p-0.5 rounded hover:bg-emerald-100 cursor-pointer"
                          aria-label="Dismiss success message"
                      >
                        <X className="w-3.5 h-3.5" />
                      </button>
                    </div>
                )}

                {/* Action Toolbar */}
                <div className="flex flex-wrap items-center justify-between gap-4 pt-4 border-t border-slate-100">
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    <Cpu className="w-3.5 h-3.5 text-indigo-500" />
                    <span>Engine: </span>
                    <span className={`font-semibold capitalize px-1.5 py-0.5 rounded text-[11px] border ${
                        activeEngine === 'neural'
                            ? 'bg-purple-50 text-purple-700 border-purple-200'
                            : 'bg-amber-50 text-amber-700 border-amber-200'
                    }`}>
                    {activeEngine}
                  </span>
                    <span className="text-slate-300">•</span>
                    <span className="text-slate-400">Press Ctrl+Enter to synthesize</span>
                  </div>

                  <div className="flex items-center gap-3 w-full sm:w-auto">
                    <GenerateButton
                        onClick={handleGenerate}
                        isLoading={isLoading}
                        disabled={text.trim().length === 0 || text.length > MAX_CHAR_LIMIT}
                        charCount={text.length}
                    />
                  </div>
                </div>
              </div>

              {/* Generated Audio Player Section */}
              {currentAudio && (
                  <div className="animate-in fade-in slide-in-from-top-3 duration-300">
                    <AudioPlayer
                        audioUrl={currentAudio.audioUrl}
                        filename={`tts-${language}-${currentVoiceObj?.name || 'speech'}.${currentAudio.format || 'mp3'}`}
                        language={language}
                        voiceName={currentVoiceObj?.name}
                        format={currentAudio.format || 'mp3'}
                        onDelete={handleDeleteCurrentAudio}
                        isDeleting={isDeletingAudio}
                    />
                  </div>
              )}
            </div>

            {/* Right Column: Status & Speech History (4 cols on desktop) */}
            <div className="lg:col-span-4 flex flex-col gap-6">
              {/* Backend Status Panel */}
              <div id="backend-status-panel" className="bg-white rounded-2xl border border-slate-200/90 p-5 shadow-xs flex flex-col gap-3">
                <div className="flex items-center justify-between">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
                    <Server className="w-3.5 h-3.5 text-indigo-600" />
                    <span>Backend Status</span>
                  </h3>
                  <div className="flex items-center gap-2">
                    <button
                        type="button"
                        onClick={handleRefreshHealth}
                        disabled={isRefreshingHealth}
                        className="p-1 text-slate-400 hover:text-indigo-600 rounded hover:bg-slate-50 transition-colors cursor-pointer disabled:opacity-50"
                        title="Refresh status"
                        aria-label="Refresh backend status"
                    >
                      <RefreshCw className={`w-3.5 h-3.5 ${isRefreshingHealth ? 'animate-spin text-indigo-600' : ''}`} />
                    </button>
                    <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${BACKEND_STATUS_DISPLAY[backendStatus].badgeClass}`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${BACKEND_STATUS_DISPLAY[backendStatus].dotClass}`} />
                      {BACKEND_STATUS_DISPLAY[backendStatus].label}
                  </span>
                  </div>
                </div>

                <div className="space-y-2 text-xs text-slate-600 pt-1">
                  <div className="flex items-center justify-between py-1.5 border-b border-slate-100">
                    <span className="text-slate-500">Service</span>
                    <span className="font-semibold text-slate-800">Spring Boot REST API</span>
                  </div>
                  <div className="flex items-center justify-between py-1.5 border-b border-slate-100">
                    <span className="text-slate-500">TTS Engine</span>
                    <span className="font-semibold text-slate-800">
                    {health?.provider || 'AWS Amazon Polly'}
                  </span>
                  </div>
                  <div className="flex items-center justify-between py-1.5 border-b border-slate-100">
                    <span className="text-slate-500">AWS Region</span>
                    <span className="font-mono font-medium text-slate-700">
                    {health?.details?.region || 'ap-northeast-2'}
                  </span>
                  </div>
                  <div className="flex items-center justify-between py-1.5 border-b border-slate-100">
                    <span className="text-slate-500">Database</span>
                    <span className="inline-flex items-center gap-1 font-semibold text-emerald-700">
                    <Database className="w-3 h-3" />
                    PostgreSQL
                  </span>
                  </div>
                  <div className="flex items-center justify-between py-1.5">
                    <span className="text-slate-500">Max Character Limit</span>
                    <span className="font-semibold text-slate-800">
                    {health?.details?.maxTextLength ? `${health.details.maxTextLength.toLocaleString()} chars` : '5,000 chars'}
                  </span>
                  </div>
                </div>
              </div>

              {/* Speech Generation History Card */}
              <div className="bg-white rounded-2xl border border-slate-200/90 p-5 shadow-xs">
                <SpeechHistoryList
                    items={history}
                    onSelect={handleSelectHistoryItem}
                    onDelete={handleDeleteHistory}
                    isLoading={isLoading}
                />
              </div>
            </div>
          </div>
        </main>

        {/* Clean, Professional Footer */}
        <footer className="border-t border-slate-200 bg-white py-6 mt-10 text-center text-xs text-slate-500">
          <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
            <p className="text-slate-600 font-medium">
              Text-to-Speech Studio • Powered by AWS Amazon Polly &amp; Spring Boot REST API
            </p>
            <div className="flex items-center gap-2 text-slate-400 text-[11px]">
              <span>PostgreSQL Persistence</span>
              <span>•</span>
              <span>Multi-Language Neural Voices</span>
              <span>•</span>
              <span>MP3 Audio Streaming</span>
            </div>
          </div>
        </footer>
      </div>
  );
}