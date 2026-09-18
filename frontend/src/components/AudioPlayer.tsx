import React, { useState, useRef, useEffect } from 'react';
import { Play, Pause, RotateCcw, Volume2, VolumeX, Download, Music, Trash2, AlertTriangle } from 'lucide-react';

interface AudioPlayerProps {
  audioUrl: string;
  filename?: string;
  language?: string;
  voiceName?: string;
  format?: string;
  onDownload?: () => void;
  onDelete?: () => void;
  isDeleting?: boolean;
}

export const AudioPlayer: React.FC<AudioPlayerProps> = ({
  audioUrl,
  filename = 'generated-speech.mp3',
  language,
  voiceName,
  format = 'mp3',
  onDelete,
  isDeleting = false,
}) => {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(0.85);
  const [isMuted, setIsMuted] = useState(false);
  const [playbackRate, setPlaybackRate] = useState(1.0);
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    audio.pause();
    audio.currentTime = 0;
    setIsPlaying(false);
    setCurrentTime(0);

    const handleLoadedMetadata = () => {
      setDuration(audio.duration || 0);
    };

    const handleTimeUpdate = () => {
      setCurrentTime(audio.currentTime);
    };

    const handleEnded = () => {
      setIsPlaying(false);
      setCurrentTime(0);
    };

    audio.addEventListener('loadedmetadata', handleLoadedMetadata);
    audio.addEventListener('timeupdate', handleTimeUpdate);
    audio.addEventListener('ended', handleEnded);

    // Auto-play newly generated audio
    const playPromise = audio.play();
    if (playPromise !== undefined) {
      playPromise
        .then(() => setIsPlaying(true))
        .catch(() => {
          // Autoplay blocked by browser policy until interaction
          setIsPlaying(false);
        });
    }

    return () => {
      audio.removeEventListener('loadedmetadata', handleLoadedMetadata);
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      audio.removeEventListener('ended', handleEnded);
    };
  }, [audioUrl]);

  const togglePlay = () => {
    const audio = audioRef.current;
    if (!audio) return;

    if (isPlaying) {
      audio.pause();
      setIsPlaying(false);
    } else {
      audio.play().then(() => setIsPlaying(true)).catch(console.error);
    }
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    const time = parseFloat(e.target.value);
    setCurrentTime(time);
    if (audioRef.current) {
      audioRef.current.currentTime = time;
    }
  };

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    setIsMuted(val === 0);
    if (audioRef.current) {
      audioRef.current.volume = val;
    }
  };

  const toggleMute = () => {
    if (audioRef.current) {
      if (isMuted) {
        audioRef.current.volume = volume || 0.8;
        setIsMuted(false);
      } else {
        audioRef.current.volume = 0;
        setIsMuted(true);
      }
    }
  };

  const changePlaybackRate = (rate: number) => {
    setPlaybackRate(rate);
    if (audioRef.current) {
      audioRef.current.playbackRate = rate;
    }
  };

  const restartAudio = () => {
    if (audioRef.current) {
      audioRef.current.currentTime = 0;
      audioRef.current.play().then(() => setIsPlaying(true)).catch(console.error);
    }
  };

  const formatTime = (seconds: number) => {
    if (isNaN(seconds) || seconds < 0) return '00:00';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const progressPercent = duration > 0 ? (currentTime / duration) * 100 : 0;

  return (
    <div id="audio-player" className="w-full bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs">
      <audio ref={audioRef} src={audioUrl} preload="metadata" />

      {/* Header with audio metadata & download button */}
      <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-600 shrink-0">
            <Music className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-semibold text-slate-800">Generated Speech Audio</h4>
            <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500 mt-0.5">
              {voiceName && <span>Voice: <strong className="text-slate-700 font-semibold">{voiceName}</strong></span>}
              {language && <span>• Lang: <strong className="text-slate-700 font-semibold">{language}</strong></span>}
              <span className="uppercase font-semibold px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 text-[10px] border border-indigo-100">
                {format}
              </span>
            </div>
          </div>
        </div>

        {/* Action buttons: Download & Delete Audio */}
        <div className="flex items-center gap-2 ml-auto">
          {/* Download direct link */}
          <a
            id="audio-download-btn"
            href={audioUrl}
            download={filename}
            className="inline-flex items-center gap-1.5 h-9 px-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-semibold shadow-xs transition-colors cursor-pointer"
            title="Download audio file"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Download Audio ({format.toUpperCase()})</span>
          </a>

          {/* Delete Audio button */}
          {onDelete && (
            <button
              id="audio-delete-btn"
              type="button"
              onClick={() => setShowConfirmDelete(true)}
              disabled={isDeleting}
              className="inline-flex items-center gap-1.5 h-9 px-3 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-700 hover:text-rose-800 border border-rose-200/80 text-xs font-semibold shadow-xs transition-colors cursor-pointer disabled:opacity-50"
              title="Delete this audio"
              aria-label="Delete Audio"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>Delete Audio</span>
            </button>
          )}
        </div>
      </div>

      {/* Confirmation Dialog / Banner */}
      {showConfirmDelete && (
        <div
          id="delete-audio-confirm-banner"
          role="alertdialog"
          aria-modal="true"
          className="mt-3 p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-950 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 animate-in fade-in duration-200"
        >
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-rose-100 text-rose-600 flex items-center justify-center shrink-0">
              <AlertTriangle className="w-4 h-4" />
            </div>
            <div>
              <p className="text-xs font-semibold text-rose-900">
                Are you sure you want to delete this generated audio?
              </p>
              <p className="text-[11px] text-rose-700">
                This will remove the audio player and delete the record from speech history.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
            <button
              id="confirm-delete-cancel-btn"
              type="button"
              onClick={() => setShowConfirmDelete(false)}
              disabled={isDeleting}
              className="px-3 py-1.5 rounded-lg bg-white hover:bg-slate-50 border border-slate-200 text-slate-700 text-xs font-medium cursor-pointer transition-colors"
            >
              Cancel
            </button>
            <button
              id="confirm-delete-submit-btn"
              type="button"
              onClick={() => {
                setShowConfirmDelete(false);
                if (onDelete) onDelete();
              }}
              disabled={isDeleting}
              className="px-3 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold shadow-xs cursor-pointer transition-colors disabled:opacity-50 inline-flex items-center gap-1.5"
            >
              {isDeleting ? (
                <>
                  <span className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Deleting...</span>
                </>
              ) : (
                <>
                  <Trash2 className="w-3 h-3" />
                  <span>Yes, Delete</span>
                </>
              )}
            </button>
          </div>
        </div>
      )}

      {/* Visualizer bars */}
      <div className="py-4 flex items-center justify-center gap-1 h-14">
        {[40, 65, 30, 85, 45, 95, 60, 35, 75, 50, 90, 40, 70, 55, 80, 45, 90, 60, 30, 85, 50, 65, 40, 75].map(
          (height, idx) => {
            const isActive = (idx / 24) * 100 <= progressPercent;
            return (
              <div
                key={idx}
                className={`w-1.5 rounded-full transition-all duration-150 ${
                  isActive
                    ? 'bg-indigo-600 shadow-xs'
                    : 'bg-slate-200'
                }`}
                style={{
                  height: isPlaying ? `${Math.max(12, height * (0.6 + Math.random() * 0.4))}%` : `${height * 0.6}%`,
                }}
              />
            );
          }
        )}
      </div>

      {/* Progress Bar & Seek */}
      <div className="flex flex-col gap-1.5">
        <input
          type="range"
          min={0}
          max={duration || 100}
          step={0.01}
          value={currentTime}
          onChange={handleSeek}
          className="w-full h-2 bg-slate-100 rounded-lg appearance-none cursor-pointer accent-indigo-600 focus:outline-none"
        />
        <div className="flex justify-between text-xs text-slate-500 font-mono">
          <span>{formatTime(currentTime)}</span>
          <span>{formatTime(duration)}</span>
        </div>
      </div>

      {/* Controls Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-4 mt-2 border-t border-slate-100">
        <div className="flex items-center gap-2">
          {/* Play/Pause Button */}
          <button
            type="button"
            onClick={togglePlay}
            className="w-10 h-10 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white flex items-center justify-center transition-transform active:scale-95 shadow-sm cursor-pointer"
            aria-label={isPlaying ? 'Pause' : 'Play'}
          >
            {isPlaying ? <Pause className="w-4 h-4 fill-current" /> : <Play className="w-4 h-4 fill-current ml-0.5" />}
          </button>

          {/* Replay Button */}
          <button
            type="button"
            onClick={restartAudio}
            className="w-9 h-9 rounded-xl hover:bg-slate-100 text-slate-600 flex items-center justify-center transition-colors cursor-pointer"
            title="Restart"
          >
            <RotateCcw className="w-4 h-4" />
          </button>

          {/* Speed Selector */}
          <div className="flex items-center rounded-lg bg-slate-100 p-0.5 text-xs font-medium text-slate-600">
            {[0.75, 1.0, 1.25, 1.5].map((rate) => (
              <button
                key={rate}
                type="button"
                onClick={() => changePlaybackRate(rate)}
                className={`px-2 py-1 rounded-md transition-colors cursor-pointer ${
                  playbackRate === rate ? 'bg-white text-indigo-700 font-bold shadow-xs' : 'hover:text-slate-900'
                }`}
              >
                {rate}x
              </button>
            ))}
          </div>
        </div>

        {/* Volume Control */}
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={toggleMute}
            className="text-slate-500 hover:text-slate-800 transition-colors cursor-pointer"
            title={isMuted ? 'Unmute' : 'Mute'}
          >
            {isMuted || volume === 0 ? <VolumeX className="w-4 h-4 text-rose-500" /> : <Volume2 className="w-4 h-4" />}
          </button>
          <input
            type="range"
            min={0}
            max={1}
            step={0.05}
            value={isMuted ? 0 : volume}
            onChange={handleVolumeChange}
            className="w-20 sm:w-24 h-1.5 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-indigo-600"
            title={`Volume: ${Math.round(volume * 100)}%`}
          />
        </div>
      </div>
    </div>
  );
};
